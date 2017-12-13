package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.CommonRepository


@Component
class Router(val repository: CommonRepository, val executors: List<Executor>) {


    fun processUpdate(update: Update): SendMessage? {
        register(update)
        val executor = executors.find { it.canExecute(update) }?:executors.find { it is DefaultExecutor }!!
        return executor.execute(update)
    }


    private fun register(update: Update) {
        val message = update.message?:update.editedMessage
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