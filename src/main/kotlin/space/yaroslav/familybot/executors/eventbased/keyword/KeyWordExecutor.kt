package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import java.util.concurrent.ThreadLocalRandom

@Component
class KeyWordExecutor(val processors: List<KeyWordProcessor>) : Executor, Configurable {

    private val log = getLogger()

    private val processorsForMessage = HashMap<Int, KeyWordProcessor>()

    override fun priority(update: Update) = Priority.LOW

    override fun getFunctionId() = FunctionId.TALK_BACK

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return processorsForMessage.remove(update.message.messageId)?.process(update) ?: {}
    }

    override fun canExecute(message: Message): Boolean {
        if (message.from.isBot) {
            return false
        }
        val keyWordProcessor = processors
            .find { it.canProcess(message) }
            ?.takeIf { isPassingRandomCheck(it, message) }
        return if (keyWordProcessor != null) {
            log.info("Key word processor is found: ${keyWordProcessor::class.simpleName}")
            processorsForMessage[message.messageId] = keyWordProcessor
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
}
