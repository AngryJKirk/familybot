package dev.storozhenko.familybot.feature.talking.services.keyword.processor

import dev.storozhenko.familybot.common.extensions.sendSticker
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.stereotype.Component

@Component
class ZigaKeyWordProcessor : KeyWordProcessor {
    private val zigaStickers = listOf(Sticker.LEFT_ZIGA, Sticker.RIGHT_ZIGA)

    override fun canProcess(context: ExecutorContext): Boolean {
        val incomeSticker = context.message.sticker ?: return false
        val isRightPack = StickerPack.FAMILY_PACK.packName == incomeSticker.setName
        return isRightPack && zigaStickers.any { it.stickerEmoji == incomeSticker.emoji }
    }

    override suspend fun process(context: ExecutorContext) {
        val stickerToSend = if (context.message.sticker?.emoji == Sticker.LEFT_ZIGA.stickerEmoji) {
            Sticker.RIGHT_ZIGA
        } else {
            Sticker.LEFT_ZIGA
        }
        context.client.sendSticker(context, stickerToSend, replyToUpdate = true)
    }
}
