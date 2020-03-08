package space.yaroslav.familybot.route.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority

@Component
class KeyWordExecutor(val processors: List<KeyWordProcessor>) : Executor {

    private val actionFastAccess = HashMap<Int, (Update) -> suspend (AbsSender) -> Unit>()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return actionFastAccess.remove(update.message.messageId)?.invoke(update) ?: {}
    }

    override fun canExecute(message: Message): Boolean {
        val keyWordProcessor = processors
            .find { it.canProcess(message) }
        return if (keyWordProcessor != null) {
            actionFastAccess[message.messageId] = { update -> keyWordProcessor.process(update) }
            true
        } else {
            false
        }
    }

    override fun priority(update: Update) = Priority.LOW
}
