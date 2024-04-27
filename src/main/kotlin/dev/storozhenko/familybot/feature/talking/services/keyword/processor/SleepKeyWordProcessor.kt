package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.common.extensions.sendSticker
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.stereotype.Component

@Component
class SleepKeyWordProcessor : KeyWordProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return text.contains("спать", ignoreCase = true) ||
                text.contains("сплю", ignoreCase = true)
    }

    override suspend fun process(context: ExecutorContext) {
        context.sender.sendSticker(context, Sticker.SWEET_DREAMS, replyToUpdate = true)
    }

    override fun isRandom() = true
}
