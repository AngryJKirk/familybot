package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.context
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.executors.PrivateMessageExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.CommandByUser
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.logging.RawUpdateLogger
import dev.storozhenko.familybot.feature.logging.repos.ChatLogRepository
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import dev.storozhenko.familybot.feature.security.AntiDdosExecutor
import dev.storozhenko.familybot.feature.settings.models.CommandLimit
import dev.storozhenko.familybot.feature.settings.models.FirstBotInteraction
import dev.storozhenko.familybot.feature.settings.models.FirstTimeInChat
import dev.storozhenko.familybot.feature.settings.models.MessageCounter
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Component
class Router(
    private val repository: UserRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val executors: List<Executor>,
    private val chatLogRepository: ChatLogRepository,
    private val configureRepository: FunctionsConfigureRepository,
    private val rawUpdateLogger: RawUpdateLogger,
    private val botConfig: BotConfig,
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService,
) {

    private val logger = KotlinLogging.logger { }
    private val chatLogRegex = Regex("[а-яА-Яё\\s,.!?]+")
    private val loggingScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val loggingExceptionHandler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception in logging job" }
    }

    suspend fun processUpdate(update: Update, sender: AbsSender) {
        val message = update.message
            ?: update.editedMessage
            ?: update.callbackQuery.message

        val chat = message.chat

        val isGroup = chat.isSuperGroupChat || chat.isGroupChat
        if (!isGroup) {
            logger.warn { "Someone is sending private messages: $update" }
        } else {
            registerUpdate(message, update)
            if (update.hasEditedMessage()) {
                return
            }
        }
        val context = update.context(botConfig, dictionary, sender)

        val executor = if (isGroup) {
            selectExecutor(context) ?: selectRandom(context)
        } else {
            selectExecutor(context, forSingleUser = true) ?: return
        }

        logger.info { "Executor to apply: ${executor.javaClass.simpleName}" }

        if (isExecutorDisabled(executor, context)) {
            when (executor) {
                is CommandExecutor -> disabledCommand(context)
                is AntiDdosExecutor -> antiDdosSkip(context)
                else -> {}
            }
        } else {
            executor.execute(context)
        }.also {
            loggingScope.launch(loggingExceptionHandler) {
                logChatCommand(executor, context)
            }
        }
    }

    private fun registerUpdate(
        message: Message,
        update: Update,
    ) {
        register(message)
        loggingScope.launch(loggingExceptionHandler) {
            rawUpdateLogger.log(update)
        }
    }

    private fun selectRandom(context: ExecutorContext): Executor {
        logger.info { "No executor found, trying to find random priority executors" }

        loggingScope.launch(loggingExceptionHandler) {
            logChatMessage(context)
        }
        val executor = executors
            .filter { it.priority(context) == Priority.RANDOM }
            .random()

        logger.info { "Random priority executor ${executor.javaClass.simpleName} was selected" }
        return executor
    }

    private suspend fun antiDdosSkip(context: ExecutorContext) {
        val executor = executors
            .filterIsInstance<CommandExecutor>()
            .find { it.canExecute(context) } ?: return
        if (isExecutorDisabled(executor, context)) {
            disabledCommand(context)
        } else {
            executor.execute(context)
        }
    }

    private suspend fun disabledCommand(context: ExecutorContext) {
        val phrase = context.phrase(Phrase.COMMAND_IS_OFF)
        context.sender.send(context, phrase)
    }

    private fun isExecutorDisabled(executor: Executor, context: ExecutorContext): Boolean {
        if (executor !is Configurable) return false

        val functionId = executor.getFunctionId(context)
        val isExecutorDisabled = !configureRepository.isEnabled(functionId, context.chat)

        if (isExecutorDisabled) {
            logger.info { "Executor ${executor::class.simpleName} is disabled" }
        }
        return isExecutorDisabled
    }

    private fun logChatCommand(executor: Executor, context: ExecutorContext) {
        if (executor is CommandExecutor && executor.isLoggable()) {
            val key = context.userAndChatKey
            val currentValue = easyKeyValueService.get(CommandLimit, key)
            if (currentValue == null) {
                easyKeyValueService.put(CommandLimit, key, 1, 5.minutes)
            } else {
                easyKeyValueService.increment(CommandLimit, key)
            }

            commandHistoryRepository.add(
                CommandByUser(
                    context.user,
                    executor.command(),
                    Instant.now(),
                ),
            )
        }
    }

    private fun logChatMessage(context: ExecutorContext) {
        if (context.message.forwardFrom != null) {
            return
        }
        val key = context.userAndChatKey
        if (easyKeyValueService.get(MessageCounter, key) != null) {
            easyKeyValueService.increment(MessageCounter, key)
        }

        val text = context.message.text
            ?.takeIf { botConfig.botNameAliases.none { alias -> it.contains(alias, ignoreCase = true) } }
            ?.takeIf { it.split(" ").size in (3..7) }
            ?.takeIf { it.length < 600 }
            ?.takeIf { chatLogRegex.matches(it) } ?: return

        chatLogRepository.add(context.user, text)
    }

    private fun selectExecutor(
        context: ExecutorContext,
        forSingleUser: Boolean = false,
    ): Executor? {
        val executorsToProcess = if (forSingleUser) {
            executors.filterIsInstance<PrivateMessageExecutor>()
        } else {
            executors.filterNot { it is PrivateMessageExecutor }
        }
        return executorsToProcess
            .asSequence()
            .map { executor -> executor to executor.priority(context) }
            .filter { (_, priority) -> priority higherThan Priority.RANDOM }
            .sortedByDescending { (_, priority) -> priority.priorityValue }
            .map { (executor, _) -> executor }
            .find { executor ->
                executor.canExecute(context)
            }
    }

    private fun register(message: Message) {
        val chat = message.chat.toChat()

        repository.addChat(chat)
        val key = chat.key()
        val firstBotInteractionDate = easyKeyValueService.get(FirstBotInteraction, key)
        if (firstBotInteractionDate == null) {
            easyKeyValueService.put(FirstBotInteraction, key, Instant.now().prettyFormat())
            easyKeyValueService.put(FirstTimeInChat, key, true, 1.days)
        }
        val leftChatMember = message.leftChatMember
        val newChatMembers = message.newChatMembers

        when {
            leftChatMember != null -> {
                if (leftChatMember.isBot && leftChatMember.userName == botConfig.botName) {
                    logger.info { "Bot was removed from $chat" }
                    repository.changeChatActiveStatus(chat, false)
                    repository.disableUsersInChat(chat)
                } else {
                    logger.info { "User $leftChatMember has left" }
                    repository.changeUserActiveStatusNew(leftChatMember.toUser(chat = chat), false)
                }
            }

            newChatMembers?.isNotEmpty() == true -> {
                if (newChatMembers.any { it.isBot && it.userName == botConfig.botName }) {
                    logger.info { "Bot was added to $chat" }
                    repository.changeChatActiveStatus(chat, true)
                } else {
                    logger.info { "New users was added: $newChatMembers" }
                    newChatMembers
                        .filterNot(User::getIsBot)
                        .forEach { repository.addUser(it.toUser(chat = chat)) }
                }
            }

            message.from.isBot.not() || message.from.userName == "GroupAnonymousBot" -> {
                repository.addUser(message.from.toUser(chat = chat))
            }
        }
    }
}
