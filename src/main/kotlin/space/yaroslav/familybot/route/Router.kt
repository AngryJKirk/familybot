package space.yaroslav.familybot.route

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.continious.ContiniousConversation
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.models.Priority
import java.time.Instant


@Component
class Router(val repository: CommonRepository,
             val historyRepository: HistoryRepository,
             val executors: List<Executor>,
             val continious: List<ContiniousConversation>,
             val chatLogRepository: ChatLogRepository,
             val configureRepository: FunctionsConfigureRepository) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)

    fun processUpdate(update: Update, me: User): (AbsSender) -> Unit {

        val message = update.message ?: update.editedMessage

        val chat = message.chat

        if (!(chat.isGroupChat || chat.isSuperGroupChat)) {
            return {}
        }

        register(message)

        if (message.isReply && message.replyToMessage.from.id == me.id) {
            val continiousResult = continious
                    .find { it.canProcessContinious(update) }
                    ?.processContinious(update)
            if (continiousResult != null) {
                return continiousResult
            }
        }

        var executor = selectExecutor(update)

        logger.info("Executor to apply: ${executor?.javaClass?.simpleName ?: "[None]"}")

        if (executor == null) {
            logger.info("No executor found, trying to apply low priority executors")

            logChatMessage(update)

            executor = selectExecutorLowPriority(update)

            logger.info("Low priority executor ${executor.javaClass.simpleName} was selected")
        }

        try {
            if (executor is Configurable && !configureRepository.isEnabled(executor.getFunctionId(), chat.toChat())) {
                return when (executor) {
                    is CommandExecutor -> { it -> it.execute(SendMessage(chat.id, "Команда выключена, сорян")) }
                    else -> { _ -> }
                }
            }
            return executor.execute(update)
        } finally {
            logChatCommand(executor, update)
        }

    }

    private fun logChatCommand(executor: Executor?, update: Update) {
        if (executor is CommandExecutor) {
            historyRepository.add(CommandByUser(
                    update.toUser(),
                    executor.command(),
                    Instant.now()))
        }
    }

    private fun selectExecutorLowPriority(update: Update): Executor {
        return executors.filter { it.priority(update) == Priority.LOW }.random()!!
    }

    private fun logChatMessage(update: Update) {
        val text = update.message?.text
        if (text != null && text.split(" ").size >= 3) {
            chatLogRepository.add(update.toUser(), text)
        }
    }

    private fun selectExecutor(update: Update): Executor? {
        return executors
                .sortedByDescending { it.priority(update).int }
                .filter { it.priority(update).int >= 0 }
                .find { it.canExecute(update.message) }
    }


    private fun register(message: Message) {
        val chat = message.chat.toChat()

        if (!repository.containsChat(chat)) {
            repository.addChat(chat)
        }

        if (message.leftChatMember != null) {
            repository.changeUserActiveStatus(message.leftChatMember.toUser(chat = chat), false)
        } else if (message.newChatMembers?.isNotEmpty() == true) {
            message.newChatMembers.forEach { repository.addUser(it.toUser(chat = chat)) }
        } else if (!message.from.bot) {
            repository.addUser(message.from.toUser(chat = chat))
        }
    }
}