package dev.storozhenko.familybot.feature.talking.services.keyword

import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.routers.models.Priority
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

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
