package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import java.util.concurrent.ThreadLocalRandom

@Component
class DefaultExecutor : Executor {
    override fun execute(update: Update): (AbsSender) -> Unit {
        if(ThreadLocalRandom.current().nextInt(0, 5) == 3){

            val sendMessage = SendMessage(update.message.chatId, "Ну ты и пидор")
            sendMessage.replyToMessageId = update.message.messageId
            return { it -> it.execute(sendMessage) }
        }
        return {  }
    }

    override fun canExecute(message: Message): Boolean {
        return false
    }
}