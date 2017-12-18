package space.yaroslav.familybot.route

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.random
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.executors.command.CommandExecutor
import space.yaroslav.familybot.route.models.Priority
import java.time.Instant


@Component
class Router(val repository: CommonRepository, val historyRepository: HistoryRepository, val executors: List<Executor>) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)
    fun processUpdate(update: Update): (AbsSender) -> Unit {
        if (!update.message.chat.isSuperGroupChat) {
            return {}
        }
        val message = update.message ?: update.editedMessage
        register(message)
        val executor = executors
                .sortedByDescending { it.priority().int }
                .filter { it.priority().int >= 0 }
                .find { it ->
                    val canExecute = it.canExecute(message)
                    logger.info("Checking ${it::class.simpleName}, result is $canExecute")
                    canExecute
                } ?: executors.filter { it.priority() == Priority.LOW }.random()
        if (executor is CommandExecutor) {
            historyRepository.add(CommandByUser(
                    message.from.toUser(telegramChat = message.chat),
                    executor.command(),
                    Instant.now()))
        }
        return executor!!.execute(update)
    }


    private fun register(message: Message) {
        registerChat(message.chat.toChat())
        message.from
                .takeIf { !it.bot }
                ?.toUser(telegramChat = message.chat)
                ?.let(repository::addUser)
    }

    private fun registerChat(chat: Chat) {
        if (!repository.containsChat(chat)) {
            repository.addChat(chat)
        }
    }

}