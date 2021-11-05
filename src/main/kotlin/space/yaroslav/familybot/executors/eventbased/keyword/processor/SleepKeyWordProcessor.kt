package space.yaroslav.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.sendSticker
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordProcessor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.stickers.Sticker

@Component
class SleepKeyWordProcessor : KeyWordProcessor {
    override fun canProcess(executorContext: ExecutorContext): Boolean {
        val text = executorContext.message.text ?: return false
        return text.contains("спать") || text.contains("сплю")
    }

    override fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return { it.sendSticker(executorContext, Sticker.SWEET_DREAMS, replyToUpdate = true) }
    }

    override fun isRandom(executorContext: ExecutorContext) = true
}
