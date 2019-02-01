package space.yaroslav.familybot.route

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.isGroup
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.executors.eventbased.AntiDdosExecutor
import space.yaroslav.familybot.route.executors.pm.PrivateMessageExecutor
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.RawUpdateLogger
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Instant

@Component
class Router(
    val repository: CommonRepository,
    val commandHistoryRepository: CommandHistoryRepository,
    val executors: List<Executor>,
    val chatLogRepository: ChatLogRepository,
    val configureRepository: FunctionsConfigureRepository,
    val rawUpdateLogger: RawUpdateLogger,
    val botConfig: BotConfig,
    val dictionary: Dictionary
) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)

    fun processUpdate(update: Update): (AbsSender) -> Unit {

        val message = update.message
            ?: update.editedMessage
            ?: update.callbackQuery.message

        val chat = message.chat

        val isGroup = chat.isGroup()
        if (!isGroup) {
            logger.warn("Someone try to do from outside of groups: $update")
        } else {
            GlobalScope.launch {
                register(message)
                rawUpdateLogger.log(update)
            }

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
                else -> { _ -> logger.info("Skip event executor due to configuration") }
            }
        } else {
            executor.execute(update)
        }.also { GlobalScope.launch { logChatCommand(executor, update) } }
    }

    private fun selectRandom(update: Update): Executor {
        logger.info("No executor found, trying to find random priority executors")

        GlobalScope.launch { logChatMessage(update) }
        val executor = executors.filter { it.priority(update) == Priority.RANDOM }.random()!!

        logger.info("Random priority executor ${executor.javaClass.simpleName} was selected")
        return executor
    }

    private fun antiDdosSkip(message: Message, update: Update): (AbsSender) -> Unit = { it ->
        logger.info("Skip anti-ddos executor due to configuration")
        val executor = executors
            .filter { it is CommandExecutor }
            .find { it.canExecute(message) }
        val function = if (isExecutorDisabled(executor, message.chat)) {
            disabledCommand(message.chat)
        } else {
            executor?.execute(update)
        }

        function?.invoke(it)
    }

    private fun disabledCommand(chat: Chat): (AbsSender) -> Unit = { it ->
        logger.info("Skip command executor due to configuration")
        it.execute(SendMessage(chat.id, dictionary.get(Phrase.COMMAND_IS_OFF)))
    }

    private fun isExecutorDisabled(executor: Executor?, chat: Chat): Boolean {
        return executor is Configurable && !configureRepository.isEnabled(executor.getFunctionId(), chat.toChat())
    }

    private fun logChatCommand(executor: Executor?, update: Update) {
        if (executor is CommandExecutor && executor.isLoggable()) {
            commandHistoryRepository.add(
                CommandByUser(
                    update.toUser(),
                    executor.command(),
                    Instant.now()
                )
            )
        }
    }

    private fun logChatMessage(update: Update) {
        val text = update.message?.text
        if (update.message?.isUserMessage == true &&
            text != null &&
            text.split(" ").size >= 3 &&
            text.length < 600 &&
            text.contains("http", ignoreCase = true)
        ) {
            chatLogRepository.add(update.toUser(), text)
        }
    }

    private fun selectExecutor(update: Update, forSingleUser: Boolean = false): Executor? {
        val executorsToProcess = if (forSingleUser) {
            executors.filter { it is PrivateMessageExecutor }
        } else {
            executors.filterNot { it is PrivateMessageExecutor }
        }
        return executorsToProcess
            .sortedByDescending { it.priority(update).int }
            .filter { it.priority(update).int > Priority.RANDOM.int }
            .find { it.canExecute(update.message ?: update.editedMessage ?: update.callbackQuery.message) }
    }

    private fun register(message: Message) {
        val chat = message.chat.toChat()

        repository.addChat(chat)
        val leftChatMember = message.leftChatMember
        val newChatMembers = message.newChatMembers

        when {
            leftChatMember != null -> {
                if (leftChatMember.bot && leftChatMember.userName == botConfig.botname) {
                    logger.info("Bot was removed from $chat")
                    repository.changeChatActiveStatus(chat, false)
                    repository.disableUsersInChat(chat)
                } else {
                    logger.info("User $leftChatMember has left")
                    repository.changeUserActiveStatusNew(leftChatMember.toUser(chat = chat), false)
                }
            }
            newChatMembers?.isNotEmpty() == true -> {
                if (newChatMembers.any { it.bot && it.userName == botConfig.botname }) {
                    logger.info("Bot was added to $chat")
                    repository.changeChatActiveStatus(chat, true)
                } else {
                    logger.info("New users was added: $newChatMembers")
                    newChatMembers.filter { !it.bot }.forEach { repository.addUser(it.toUser(chat = chat)) }
                }
            }
            message.from.bot.not() -> {
                repository.addUser(message.from.toUser(chat = chat))
            }
        }
    }
}
