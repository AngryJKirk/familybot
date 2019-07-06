package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Huificator
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class HuificatorExecutor : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.HUIFICATE
    }

    override fun priority(update: Update): Priority {
        return Priority.RANDOM
    }

    override fun execute(update: Update): (AbsSender) -> Unit {

        val message = update.message
        val text = message?.text

        if (text != null && ThreadLocalRandom.current().nextInt(0, 10) == 0) {
            val huifyed = Huificator.huify(text.split(" ").last()) ?: return { }
            val sendMessage = SendMessage(message.chatId, huifyed)
            return { it -> it.execute(sendMessage) }
        } else {
            return { }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return false
    }
}
