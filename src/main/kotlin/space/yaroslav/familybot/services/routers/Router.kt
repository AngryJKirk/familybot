package space.yaroslav.familybot.services.routers

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.isGroup
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.prettyFormat
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

    suspend fun processUpdate(update: Update): suspend (AbsSender) -> Unit {

        val message = update.message
            ?: update.editedMessage
            ?: update.callbackQuery.message

        val chat = message.chat

        val isGroup = chat.isGroup()
        if (!isGroup) {
            logger.warn("Someone is sending private messages: $update")
        } else {
            registerUpdate(message, update)
            if (update.hasEditedMessage()) {
                return {}
            }
        }

        val executor = if (isGroup) {
            selectExecutor(update) ?: selectRandom(update)
        } else {
            selectExecutor(update, forSingleUser = true) ?: return {}
        }

        logger.info("Executor to apply: ${executor.javaClass.simpleName}")

        return if (isExecutorDisabled(executor, chat)) {
            when (executor) {
                is CommandExecutor -> disabledCommand(chat)
                is AntiDdosExecutor -> antiDdosSkip(message, update)
                else -> { _ -> }
            }
        } else {
            executor.meteredExecute(update, meterRegistry)
        }.also { logChatCommand(executor, update) }
    }

    private suspend fun registerUpdate(
        message: Message,
        update: Update
    ) {
        coroutineScope {
            launch {
                register(message)
                rawUpdateLogger.log(update)
            }
        }
    }

    private suspend fun selectRandom(update: Update): Executor {
        logger.info("No executor found, trying to find random priority executors")

        coroutineScope { launch { logChatMessage(update) } }
        val executor = executors.filter { it.meteredPriority(update, meterRegistry) == Priority.RANDOM }.random()

        logger.info("Random priority executor ${executor.javaClass.simpleName} was selected")
        return executor
    }

    private fun antiDdosSkip(message: Message, update: Update): suspend (AbsSender) -> Unit = marker@{ it ->
        val executor = executors
            .filterIsInstance<CommandExecutor>()
            .find { it.meteredCanExecute(message, meterRegistry) } ?: return@marker
        val function = if (isExecutorDisabled(executor, message.chat)) {
            disabledCommand(message.chat)
        } else {
            executor.meteredExecute(update, meterRegistry)
        }

        function.invoke(it)
    }

    private fun disabledCommand(chat: Chat): suspend (AbsSender) -> Unit {
        val mappedChat = chat.toChat()
        val phrase = dictionary.get(Phrase.COMMAND_IS_OFF, mappedChat.key())
        return { it ->
            it.execute(SendMessage(mappedChat.idString, phrase))
        }
    }

    private fun isExecutorDisabled(executor: Executor, chat: Chat): Boolean {
        if (executor !is Configurable) return false

        val functionId = executor.getFunctionId()
        val isExecutorDisabled = !configureRepository.isEnabled(functionId, chat.toChat())

        if (isExecutorDisabled) {
            logger.info("Executor ${executor::class.simpleName} is disabled")
        }
        return isExecutorDisabled
    }

    private suspend fun logChatCommand(executor: Executor, update: Update) {
        coroutineScope {
            launch {
                if (executor is CommandExecutor && executor.isLoggable()) {
                    val key = update.key()
                    val currentValue = easyKeyValueService.get(CommandLimit, key)
                    if (currentValue == null) {
                        easyKeyValueService.put(CommandLimit, key, 1, Duration.of(5, ChronoUnit.MINUTES))
                    } else {
                        easyKeyValueService.increment(CommandLimit, key)
                    }
                    commandHistoryRepository.add(
                        CommandByUser(
                            update.toUser(),
                            executor.command(),
                            Instant.now()
                        )
                    )
                }
            }
        }
    }

    private fun logChatMessage(update: Update) {
        val text = update.message.text
            ?.takeUnless { it.contains("сучар", ignoreCase = true) }
            ?.takeIf { it.split(" ").size >= 3 }
            ?.takeIf { it.split(" ").size < 8 }
            ?.takeIf { it.length < 600 }
            ?.takeIf { chatLogRegex.matches(it) } ?: return

        chatLogRepository.add(update.toUser(), text)
    }

    private fun selectExecutor(update: Update, forSingleUser: Boolean = false): Executor? {
        val executorsToProcess = if (forSingleUser) {
            executors.filterIsInstance<PrivateMessageExecutor>()
        } else {
            executors.filterNot { it is PrivateMessageExecutor }
        }
        return executorsToProcess
            .sortedByDescending { it.meteredPriority(update, meterRegistry).priorityValue }
            .filter { it.meteredPriority(update, meterRegistry) higherThan Priority.RANDOM }
            .find {
                it.meteredCanExecute(
                    update.message ?: update.editedMessage ?: update.callbackQuery.message,
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
                if (leftChatMember.isBot && leftChatMember.userName == botConfig.botname) {
                    logger.info("Bot was removed from $chat")
                    repository.changeChatActiveStatus(chat, false)
                    repository.disableUsersInChat(chat)
                } else {
                    logger.info("User $leftChatMember has left")
                    repository.changeUserActiveStatusNew(leftChatMember.toUser(chat = chat), false)
                }
            }
            newChatMembers?.isNotEmpty() == true -> {
                if (newChatMembers.any { it.isBot && it.userName == botConfig.botname }) {
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
