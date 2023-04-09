package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.common.extensions.sendSticker
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

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
