package space.yaroslav.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.sendSticker
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordProcessor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.models.telegram.stickers.StickerPack

@Component
class ZigaKeyWordProcessor : KeyWordProcessor {
    private val zigaStickers = listOf(Sticker.LEFT_ZIGA, Sticker.RIGHT_ZIGA)

    override fun canProcess(context: ExecutorContext): Boolean {
        val incomeSticker = context.message.sticker ?: return false
        val isRightPack = StickerPack.FAMILY_PACK.packName == incomeSticker.setName
        return isRightPack && zigaStickers.any { it.stickerEmoji == incomeSticker.emoji }
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val stickerToSend = if (context.message.sticker?.emoji == Sticker.LEFT_ZIGA.stickerEmoji) {
                Sticker.RIGHT_ZIGA
            } else {
                Sticker.LEFT_ZIGA
            }
            it.sendSticker(context, stickerToSend, replyToUpdate = true)
        }
    }
}
