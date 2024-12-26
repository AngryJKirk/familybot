package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.stereotype.Component

@Component
class HelloKeyWordProcessor : KeyWordProcessor {
    private val helloStickers = listOf(Sticker.LEFT_HELLO, Sticker.RIGHT_HELLO)

    override fun canProcess(context: ExecutorContext): Boolean {
        val incomeSticker = context.message.sticker ?: return false
        val isRightPack = StickerPack.FAMILY_PACK.packName == incomeSticker.setName
        return isRightPack && helloStickers.any { it.stickerEmoji == incomeSticker.emoji }
    }

    override suspend fun process(context: ExecutorContext) {
        val stickerToSend = if (context.message.sticker?.emoji == Sticker.LEFT_HELLO.stickerEmoji) {
            Sticker.RIGHT_HELLO
        } else {
            Sticker.LEFT_HELLO
        }
        context.sendSticker(stickerToSend, replyToUpdate = true)
    }
}
