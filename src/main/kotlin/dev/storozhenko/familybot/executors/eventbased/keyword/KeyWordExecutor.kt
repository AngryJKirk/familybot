package dev.storozhenko.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.Executor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.router.Priority

@Component
class KeyWordExecutor(val processors: List<KeyWordProcessor>) : Executor, Configurable {

    private val log = getLogger()

    private val processorsForMessage = HashMap<Int, KeyWordProcessor>()

    override fun priority(context: ExecutorContext) = Priority.VERY_LOW

    override fun getFunctionId(context: ExecutorContext) = FunctionId.TALK_BACK

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return processorsForMessage.remove(context.message.messageId)?.process(context) ?: {}
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        if (message.from.isBot) {
            return false
        }
        val keyWordProcessor = processors
            .find { it.canProcess(context) }
            ?.takeIf { isPassingRandomCheck(it, context) }
        return if (keyWordProcessor != null) {
            log.info("Key word processor is found: ${keyWordProcessor::class.simpleName}")
            processorsForMessage[message.messageId] = keyWordProcessor
            true
        } else {
            false
        }
    }

    private fun isPassingRandomCheck(processor: KeyWordProcessor, context: ExecutorContext): Boolean {
        return if (processor.isRandom(context)) {
            randomInt(0, 5) == 0
        } else {
            true
        }
    }
}
