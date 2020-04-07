package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class KeyWordExecutor(val processors: List<KeyWordProcessor>) : Executor {

    private val actionFastAccess = HashMap<Int, (Update) -> suspend (AbsSender) -> Unit>()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return actionFastAccess.remove(update.message.messageId)?.invoke(update) ?: {}
    }

    override fun canExecute(message: Message): Boolean {
        val keyWordProcessor = processors
            .find { it.canProcess(message) }
            ?.takeIf { isPassingRandomCheck(it, message) }
        return if (keyWordProcessor != null) {
            actionFastAccess[message.messageId] = { update -> keyWordProcessor.process(update) }
            true
        } else {
            false
        }
    }

    private fun isPassingRandomCheck(processor: KeyWordProcessor, message: Message): Boolean {
        return if (processor.isRandom(message)) {
            ThreadLocalRandom.current().nextInt(0, 5) == 0
        } else {
            true
        }
    }

    override fun priority(update: Update) = Priority.LOW
}
