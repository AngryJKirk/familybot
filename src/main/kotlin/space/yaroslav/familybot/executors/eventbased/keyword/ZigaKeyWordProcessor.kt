package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.sendSticker
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.models.telegram.stickers.StickerPack

@Component
class ZigaKeyWordProcessor : KeyWordProcessor {
    private val zigaStickers = listOf(Sticker.LEFT_ZIGA, Sticker.RIGHT_ZIGA)

    override fun canProcess(message: Message): Boolean {
        val incomeSticker = message.sticker ?: return false
        val isRightPack = StickerPack.FAMILY_PACK.packName == incomeSticker.setName
        return isRightPack && zigaStickers.any { it.stickerEmoji == incomeSticker.emoji }
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        return {
            val stickerToSend = if (update.message.sticker?.emoji == Sticker.LEFT_ZIGA.stickerEmoji) {
                Sticker.RIGHT_ZIGA
            } else {
                Sticker.LEFT_ZIGA
            }
            it.sendSticker(update, stickerToSend, replyToUpdate = true)
        }
    }

    override fun isRandom(message: Message) = false
}
