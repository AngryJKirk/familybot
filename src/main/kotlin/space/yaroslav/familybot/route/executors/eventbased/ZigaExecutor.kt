package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.sendSticker
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.models.stickers.Sticker
import space.yaroslav.familybot.route.models.stickers.StickerPack

@Component
class ZigaExecutor : Executor {
    private val zigaStickers = listOf(Sticker.LEFT_ZIGA, Sticker.RIGHT_ZIGA)

    override fun priority(update: Update): Priority {
        return Priority.LOW
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val stickerToSend = if (update.message.sticker?.emoji == Sticker.LEFT_ZIGA.stickerEmoji) {
                Sticker.RIGHT_ZIGA
            } else {
                Sticker.LEFT_ZIGA
            }
            it.sendSticker(update, stickerToSend, replyToUpdate = true)
        }
    }

    override fun canExecute(message: Message): Boolean {
        val incomeSticker = message.sticker ?: return false
        val isRightPack = StickerPack.FAMILY_PACK.packName == incomeSticker.setName
        return isRightPack && zigaStickers.any { it.stickerEmoji == incomeSticker.emoji }
    }
}
