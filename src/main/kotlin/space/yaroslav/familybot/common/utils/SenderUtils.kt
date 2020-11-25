package space.yaroslav.familybot.common.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.stickers.Sticker
import space.yaroslav.familybot.models.stickers.StickerPack
import java.util.concurrent.ThreadLocalRandom
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

suspend fun AbsSender.send(
    update: Update,
    text: String,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Long, Long> = 1000L to 2000L
): Message {
    val messageObj = SendMessage(update.chatIdString(), text).apply { enableHtml(enableHtml) }

    if (replyMessageId != null) {
        messageObj.replyToMessageId = replyMessageId
    }
    if (replyToUpdate) {
        messageObj.replyToMessageId = update.message.messageId
    }
    if (shouldTypeBeforeSend) {
        this.execute(SendChatAction(update.chatIdString(), "typing"))
        delay(ThreadLocalRandom.current().nextLong(typeDelay.first, typeDelay.second))
    }

    return this.execute(messageObj.apply(customization))
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
    val sendSticker = SendSticker().apply {
        sticker = InputFile(stickerId.await()?.fileId)
        chatId = update.chatIdString()
    }
    if (replyToUpdate) {
        sendSticker.replyToMessageId = update.message.messageId
    }
    return sender.execute(sendSticker)
}
