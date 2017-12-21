package space.yaroslav.familybot.route

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.models.Priority
import java.time.Instant


@Component
class Router(val repository: CommonRepository,
             val historyRepository: HistoryRepository,
             val executors: List<Executor>,
             val chatLogRepository: ChatLogRepository) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)

    fun processUpdate(update: Update): (AbsSender) -> Unit {

        if (!update.message.chat.isSuperGroupChat) {
            return {}
        }
        val message = update.message ?: update.editedMessage

        register(message)

        var executor = selectExecutor(update)

        logger.info("Executor to apply: ${executor?.javaClass?.simpleName ?: "[None]"}")

        if (executor == null) {
            logger.info("No executor found, trying to apply low priority executors")

            logChatMessage(update)

            executor = selectExecutorLowPriority(update)

            logger.info("Low priority executor ${executor?.javaClass?.simpleName} was selected")
        }
        try {
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

        if (!message.from.bot) {
            repository.addUser(message.from.toUser(chat = chat))
        }
    }

}