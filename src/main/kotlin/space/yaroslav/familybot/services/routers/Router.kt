package space.yaroslav.familybot.services.routers

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.executorContext
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.prettyFormat
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.common.meteredCanExecute
import space.yaroslav.familybot.common.meteredExecute
import space.yaroslav.familybot.common.meteredPriority
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.executors.eventbased.AntiDdosExecutor
import space.yaroslav.familybot.executors.pm.PrivateMessageExecutor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.CommandByUser
import space.yaroslav.familybot.repos.ChatLogRepository
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.misc.RawUpdateLogger
import space.yaroslav.familybot.services.settings.CommandLimit
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.FirstBotInteraction
import space.yaroslav.familybot.services.settings.FirstTimeInChat
import space.yaroslav.familybot.services.settings.MessageCounter
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class Router(
    private val repository: CommonRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val executors: List<Executor>,
    private val chatLogRepository: ChatLogRepository,
    private val configureRepository: FunctionsConfigureRepository,
    private val rawUpdateLogger: RawUpdateLogger,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary,
    private val meterRegistry: MeterRegistry,
    private val easyKeyValueService: EasyKeyValueService
) {

    private val logger = getLogger()
    private val chatLogRegex = Regex("[а-яА-Яё\\s,.!?]+")
    private val loggingScope = CoroutineScope(Dispatchers.Default)
    suspend fun processUpdate(update: Update): suspend (AbsSender) -> Unit {

        val message = update.message
            ?: update.editedMessage
            ?: update.callbackQuery.message

        val chat = message.chat

        val isGroup = chat.isSuperGroupChat || chat.isGroupChat
        if (!isGroup) {
            logger.warn("Someone is sending private messages: $update")
        } else {
            registerUpdate(message, update)
            if (update.hasEditedMessage()) {
                return {}
            }
        }
        val executorContext = update.executorContext(botConfig, dictionary)

        val executor = if (isGroup) {
            selectExecutor(executorContext) ?: selectRandom(executorContext)
        } else {
            selectExecutor(executorContext, forSingleUser = true) ?: return {}
        }

        logger.info("Executor to apply: ${executor.javaClass.simpleName}")

        return if (isExecutorDisabled(executor, executorContext)) {
            when (executor) {
                is CommandExecutor -> disabledCommand(executorContext)
                is AntiDdosExecutor -> antiDdosSkip(executorContext)
                else -> { _ -> }
            }
        } else {
            executor.meteredExecute(executorContext, meterRegistry)
        }.also {
            loggingScope.launch {
                delay(3000)
                logChatCommand(executor, executorContext)
            }
        }
    }

    private fun registerUpdate(
        message: Message,
        update: Update
    ) {
        register(message)
        loggingScope.launch {
            delay(3000) // temporary fix in order to check why it fails sometimes
            rawUpdateLogger.log(update)
        }
    }

    private fun selectRandom(executorContext: ExecutorContext): Executor {
        logger.info("No executor found, trying to find random priority executors")

        loggingScope.launch { logChatMessage(executorContext) }
        val executor = executors
            .filter { it.meteredPriority(executorContext, meterRegistry) == Priority.RANDOM }
            .random()

        logger.info("Random priority executor ${executor.javaClass.simpleName} was selected")
        return executor
    }

    private fun antiDdosSkip(executorContext: ExecutorContext): suspend (AbsSender) -> Unit =
        marker@{ it ->
            val executor = executors
                .filterIsInstance<CommandExecutor>()
                .find { it.meteredCanExecute(executorContext, meterRegistry) } ?: return@marker
            val function = if (isExecutorDisabled(executor, executorContext)) {
                disabledCommand(executorContext)
            } else {
                executor.meteredExecute(executorContext, meterRegistry)
            }

            function.invoke(it)
        }

    private fun disabledCommand(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val phrase = executorContext.phrase(Phrase.COMMAND_IS_OFF)
        return { it -> it.send(executorContext, phrase) }
    }

    private fun isExecutorDisabled(executor: Executor, executorContext: ExecutorContext): Boolean {
        if (executor !is Configurable) return false

        val functionId = executor.getFunctionId(executorContext)
        val isExecutorDisabled = !configureRepository.isEnabled(functionId, executorContext.chat)

        if (isExecutorDisabled) {
            logger.info("Executor ${executor::class.simpleName} is disabled")
        }
        return isExecutorDisabled
    }

    private fun logChatCommand(executor: Executor, executorContext: ExecutorContext) {
        if (executor is CommandExecutor && executor.isLoggable()) {
            val key = executorContext.userAndChatKey
            val currentValue = easyKeyValueService.get(CommandLimit, key)
            if (currentValue == null) {
                easyKeyValueService.put(CommandLimit, key, 1, Duration.of(5, ChronoUnit.MINUTES))
            } else {
                easyKeyValueService.increment(CommandLimit, key)
            }

            commandHistoryRepository.add(
                CommandByUser(
                    executorContext.user,
                    executor.command(),
                    Instant.now()
                )
            )
        }
    }

    private fun logChatMessage(executorContext: ExecutorContext) {
        val key = executorContext.userAndChatKey
        if (easyKeyValueService.get(MessageCounter, key) != null) {
            easyKeyValueService.increment(MessageCounter, key)
        }

        val text = executorContext.message.text
            ?.takeIf { botConfig.botNameAliases.none { alias -> it.contains(alias, ignoreCase = true) } }
            ?.takeIf { it.split(" ").size in (3..7) }
            ?.takeIf { it.length < 600 }
            ?.takeIf { chatLogRegex.matches(it) } ?: return

        chatLogRepository.add(executorContext.user, text)
    }

    private fun selectExecutor(
        executorContext: ExecutorContext,
        forSingleUser: Boolean = false
    ): Executor? {
        val executorsToProcess = if (forSingleUser) {
            executors.filterIsInstance<PrivateMessageExecutor>()
        } else {
            executors.filterNot { it is PrivateMessageExecutor }
        }
        return executorsToProcess
            .sortedByDescending { it.meteredPriority(executorContext, meterRegistry).priorityValue }
            .filter { it.meteredPriority(executorContext, meterRegistry) higherThan Priority.RANDOM }
            .find {
                it.meteredCanExecute(
                    executorContext,
                    meterRegistry
                )
            }
    }

    private fun register(message: Message) {
        val chat = message.chat.toChat()

        repository.addChat(chat)
        val key = chat.key()
        val firstBotInteractionDate = easyKeyValueService.get(FirstBotInteraction, key)
        if (firstBotInteractionDate == null) {
            easyKeyValueService.put(FirstBotInteraction, key, Instant.now().prettyFormat())
            easyKeyValueService.put(FirstTimeInChat, key, true, Duration.of(1, ChronoUnit.DAYS))
        }
        val leftChatMember = message.leftChatMember
        val newChatMembers = message.newChatMembers

        when {
            leftChatMember != null -> {
                if (leftChatMember.isBot && leftChatMember.userName == botConfig.botName) {
                    logger.info("Bot was removed from $chat")
                    repository.changeChatActiveStatus(chat, false)
                    repository.disableUsersInChat(chat)
                } else {
                    logger.info("User $leftChatMember has left")
                    repository.changeUserActiveStatusNew(leftChatMember.toUser(chat = chat), false)
                }
            }
            newChatMembers?.isNotEmpty() == true -> {
                if (newChatMembers.any { it.isBot && it.userName == botConfig.botName }) {
                    logger.info("Bot was added to $chat")
                    repository.changeChatActiveStatus(chat, true)
                } else {
                    logger.info("New users was added: $newChatMembers")
                    newChatMembers.filter { !it.isBot }.forEach { repository.addUser(it.toUser(chat = chat)) }
                }
            }
            message.from.isBot.not() -> {
                repository.addUser(message.from.toUser(chat = chat))
            }
        }
    }
}
