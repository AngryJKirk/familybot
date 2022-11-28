package dev.storozhenko.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.sendSticker
import dev.storozhenko.familybot.executors.eventbased.keyword.KeyWordProcessor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.stickers.Sticker

@Component
class SleepKeyWordProcessor : KeyWordProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return text.contains("спать", ignoreCase = true) ||
            text.contains("сплю", ignoreCase = true)
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { it.sendSticker(context, Sticker.SWEET_DREAMS, replyToUpdate = true) }
    }

    override fun isRandom(context: ExecutorContext) = true
}
