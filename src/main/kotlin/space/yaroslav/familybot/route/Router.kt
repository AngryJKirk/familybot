package space.yaroslav.familybot.route

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.CommonRepository


@Component
class Router(val repository: CommonRepository, val executors: List<Executor>) {

    private final val logger = LoggerFactory.getLogger(Router::class.java)
    fun processUpdate(update: Update): (AbsSender) -> Unit {
        if(!update.message.chat.isSuperGroupChat){
            return {}
        }
        val message = update.message ?: update.editedMessage
        register(message)
        val executor = executors
                .find { it ->
                    val canExecute = it.canExecute(message)
                    logger.info("Checking ${it::class.simpleName}, result is ${canExecute}")
                    canExecute
                } ?: executors.first { it is HuificatorExecutor }

        return executor.execute(update)
    }


    private fun register(message: Message) {
        registerChat(message.chat.toChat())
        message.from
                .takeIf { !it.bot }
                ?.toUser(telegramChat = message.chat)
                ?.let(this::registerUser)
    }

    private fun registerUser(user: User) {
        if (!repository.containsUser(user)) {
            repository.addUser(user)
        }
    }

    private fun registerChat(chat: Chat) {
        if (!repository.containsChat(chat)) {
            repository.addChat(chat)
        }
    }

}