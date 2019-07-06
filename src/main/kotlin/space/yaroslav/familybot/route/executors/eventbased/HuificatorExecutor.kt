package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Huificator
import space.yaroslav.familybot.common.utils.send
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
        val text = message.text ?: return {}

        if (shouldHuificate()) {
            val huifyed = Huificator.huify(getLastWord(text)) ?: return { }
            return { it -> it.send(update, huifyed) }
        } else {
            return { }
        }
    }

    private fun getLastWord(text: String) = text.split(" ").last()

    private fun shouldHuificate() = ThreadLocalRandom.current().nextInt(0, 10) == 0

    override fun canExecute(message: Message): Boolean {
        return false
    }
}
