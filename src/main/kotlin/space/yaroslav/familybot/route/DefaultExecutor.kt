package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import java.util.concurrent.ThreadLocalRandom

@Component
class DefaultExecutor : Executor {
    override fun execute(update: Update): SendMessage? {
        if(ThreadLocalRandom.current().nextInt(0, 5) == 3){
            val sendMessage = SendMessage(update.message.chatId, "Ну ты и пидор")
            sendMessage.replyToMessageId = update.message.messageId
            return sendMessage
        }
        return null
    }

    override fun canExecute(update: Update): Boolean {
        return false
    }
}