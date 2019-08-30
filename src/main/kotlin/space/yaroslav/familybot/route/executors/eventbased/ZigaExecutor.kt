package space.yaroslav.familybot.route.executors.eventbased

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority

@Component
class ZigaExecutor : Executor {
    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    private val leftEmoji = "🖐"
    private val rightEmoji = "\uD83E\uDD1A"
    private val stickerSetName = "FamiliGayPack"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val emojiToSend = if (update.message.sticker?.emoji == leftEmoji) {
                rightEmoji
            } else {
                leftEmoji
            }
            val stickerToSend = GlobalScope.async {
                it
                    .execute(GetStickerSet(stickerSetName))
                    .stickers
                    .find { sticker -> sticker.emoji == emojiToSend }
            }
            it.execute(
                SendSticker()
                    .setChatId(update.message.chatId)
                    .setReplyToMessageId(update.message.messageId)
                    .setSticker(
                        stickerToSend.await()?.fileId
                    )
            )
        }
    }

    override fun canExecute(message: Message): Boolean {
        val sticker = message.sticker
        return (leftEmoji == sticker?.emoji || rightEmoji == sticker?.emoji) && stickerSetName == sticker.setName
    }
}
