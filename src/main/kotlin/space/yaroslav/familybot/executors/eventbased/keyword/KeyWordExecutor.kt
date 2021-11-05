package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.randomInt
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority

@Component
class KeyWordExecutor(val processors: List<KeyWordProcessor>) : Executor, Configurable {

    private val log = getLogger()

    private val processorsForMessage = HashMap<Int, KeyWordProcessor>()

    override fun priority(executorContext: ExecutorContext) = Priority.LOW

    override fun getFunctionId(executorContext: ExecutorContext) = FunctionId.TALK_BACK

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return processorsForMessage.remove(executorContext.message.messageId)?.process(executorContext) ?: {}
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        if (message.from.isBot) {
            return false
        }
        val keyWordProcessor = processors
            .find { it.canProcess(executorContext) }
            ?.takeIf { isPassingRandomCheck(it, executorContext) }
        return if (keyWordProcessor != null) {
            log.info("Key word processor is found: ${keyWordProcessor::class.simpleName}")
            processorsForMessage[message.messageId] = keyWordProcessor
            true
        } else {
            false
        }
    }

    private fun isPassingRandomCheck(processor: KeyWordProcessor, executorContext: ExecutorContext): Boolean {
        return if (processor.isRandom(executorContext)) {
            randomInt(0, 5) == 0
        } else {
            true
        }
    }
}
