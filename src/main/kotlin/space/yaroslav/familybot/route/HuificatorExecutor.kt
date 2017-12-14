package space.yaroslav.familybot.route

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Huificator
import java.util.concurrent.ThreadLocalRandom

@Component
class HuificatorExecutor : Executor {

    val huificator = Huificator()

    override fun execute(update: Update): (AbsSender) -> Unit {

        val message = update.message
        val text = message?.text

        if (text != null && ThreadLocalRandom.current().nextInt(0, 5) == 3) {
            val huifyed = huificator.huify(text.split(" ").last()) ?: return { }
            val sendMessage = SendMessage(message.chatId, huifyed)
            sendMessage.replyToMessageId = message.messageId
            return { it -> it.execute(sendMessage) }
        }
        return { }
    }

    override fun canExecute(message: Message): Boolean {
        return false
    }


}