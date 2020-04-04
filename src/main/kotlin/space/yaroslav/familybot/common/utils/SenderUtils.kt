package space.yaroslav.familybot.common.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.stickers.Sticker
import space.yaroslav.familybot.models.stickers.StickerPack

fun AbsSender.send(
    update: Update,
    text: String,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> SendMessage = { this }
): Message {
    val messageObj = SendMessage(update.chatId(), text).enableHtml(enableHtml)
    if (replyMessageId != null) {
        messageObj.replyToMessageId = replyMessageId
    }
    if (replyToUpdate) {
        messageObj.replyToMessageId = update.message.messageId
    }

    return this.execute(customization(messageObj))
}

suspend fun AbsSender.sendSticker(
    update: Update,
    sticker: Sticker,
    replyToUpdate: Boolean = false
): Message {
    return sendStickerInternal(this, update, replyToUpdate, sticker.pack) {
        find { it.emoji == sticker.stickerEmoji }
    }
}

suspend fun AbsSender.sendRandomSticker(
    update: Update,
    stickerPack: StickerPack,
    replyToUpdate: Boolean = false
): Message {
    return sendStickerInternal(this, update, replyToUpdate, stickerPack) {
        random()
    }
}

private suspend fun sendStickerInternal(
    sender: AbsSender,
    update: Update,
    replyToUpdate: Boolean = false,
    stickerPack: StickerPack,
    stickerSelector: List<TelegramSticker>.() -> TelegramSticker?
): Message {
    val stickerId = GlobalScope.async {
        stickerSelector(sender.execute(GetStickerSet(stickerPack.packName)).stickers)
    }
    val sendSticker = SendSticker()
        .setSticker(stickerId.await()?.fileId)
        .setChatId(update.chatId())
    if (replyToUpdate) {
        sendSticker.replyToMessageId = update.message.messageId
    }
    return sender.execute(sendSticker)
}
