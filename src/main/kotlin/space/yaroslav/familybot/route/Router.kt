package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.*


@Component
class Router(val repository: Repository, val executors: List<Executor>) {


    fun processUpdate(update: Update): SendMessage? {
        register(update)
        val executor = executors.find { it.canExecute(update) }?:executors.find { it is DefaultExecutor }


//        if (update.message.text == "/pidor") {
//            val chat = update.message.chat.toChat()
//            val pidor = repository
//                    .getPidorsByChat(chat).find { it.date.isToday() }
//            val message = pidor?.let { SendMessage(update.message.chatId, "Сегодняшний пидор уже обнаружен: @${it.user.nickname}") }
//            return if (message != null) {
//                message
//            } else{
//                val users = repository.getUsers(chat)
//                val id = ThreadLocalRandom.current().nextInt(0, users.size)
//                val nextPidor = users[id]
//                repository.addPidor(Pidor(1, nextPidor, LocalDateTime.now()))
//                SendMessage(update.message.chatId, "Пидор это @${nextPidor.nickname}")
//            }
//        }
//
//        val from = update.message.from
//        return SendMessage(update.message.chatId, "Пошел нахуй, @${from.userName?: from.firstName + " " + from.lastName}")
    }


    private fun register(update: Update) {
        val message = update.message
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