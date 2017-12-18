package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Huificator
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class HuificatorExecutor : Executor {
    override fun priority(): Priority {
        return Priority.LOW
    }

    val huificator = Huificator()

    override fun execute(update: Update): (AbsSender) -> Unit {

        val message = update.message
        val text = message?.text

        if (text != null && ThreadLocalRandom.current().nextInt(0, 5) == 3) {
            val huifyed = huificator.huify(text.split(" ").last()) ?: return { }
            val sendMessage = SendMessage(message.chatId, huifyed)
            return { it -> it.execute(sendMessage) }
        }
        return { }
    }

    override fun canExecute(message: Message): Boolean {
        return true
    }


}