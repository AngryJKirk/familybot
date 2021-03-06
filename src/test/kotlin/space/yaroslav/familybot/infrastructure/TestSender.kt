package space.yaroslav.familybot.infrastructure

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumb
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.objects.ChatMember
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.updateshandlers.SentCallback
import java.io.Serializable
import java.util.concurrent.CompletableFuture

class TestSender(val actions: MutableList<Action<*>> = ArrayList()) : AbsSender() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Serializable?, Method : BotApiMethod<T>?> sendApiMethod(method: Method): T {
        if (method is GetChatMember) {
            return ChatMember() as T
        }
        if (method is SendMessage) {
            val action = ActionWithText(
                method.chatId,
                isHtmlEnabled = method.isHtmlEnabled(),
                replyId = method.replyToMessageId,
                content = method.text
            )
            actions.add(action)
            return Message() as T
        }
        if (method is SendSticker) {
            val action = ActionWithSticker(
                method.chatId,
                replyId = method.replyToMessageId,
                content = method.sticker
            )
            actions.add(action)
            return Message() as T
        }
        return StickerSet() as T
    }

    override fun <T : Serializable?, Method : BotApiMethod<T>?, Callback : SentCallback<T>?> sendApiMethodAsync(
        method: Method,
        callback: Callback
    ) {
        TODO("not implemented")
    }

    override fun <T : Serializable?, Method : BotApiMethod<T>?> sendApiMethodAsync(method: Method): CompletableFuture<T> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendDocument: SendDocument?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendPhoto: SendPhoto?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendVideo: SendVideo?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendVideoNote: SendVideoNote?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendSticker: SendSticker?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendAudio: SendAudio?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendVoice: SendVoice?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendMediaGroup: SendMediaGroup?): CompletableFuture<MutableList<Message>> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(setChatPhoto: SetChatPhoto?): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(addStickerToSet: AddStickerToSet?): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(setStickerSetThumb: SetStickerSetThumb?): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(createNewStickerSet: CreateNewStickerSet?): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(uploadStickerFile: UploadStickerFile?): CompletableFuture<File> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(editMessageMedia: EditMessageMedia?): CompletableFuture<Serializable> {
        TODO("Not yet implemented")
    }

    override fun executeAsync(sendAnimation: SendAnimation?): CompletableFuture<Message> {
        TODO("Not yet implemented")
    }

    override fun execute(sendDocument: SendDocument?): Message {
        TODO("not implemented")
    }

    override fun execute(sendPhoto: SendPhoto?): Message {
        TODO("not implemented")
    }

    override fun execute(sendVideo: SendVideo?): Message {
        TODO("not implemented")
    }

    override fun execute(sendVideoNote: SendVideoNote?): Message {
        TODO("not implemented")
    }

    override fun execute(sendSticker: SendSticker?): Message {
        TODO("not implemented")
    }

    override fun execute(sendAudio: SendAudio?): Message {
        TODO("not implemented")
    }

    override fun execute(sendVoice: SendVoice?): Message {
        TODO("not implemented")
    }

    override fun execute(sendMediaGroup: SendMediaGroup?): MutableList<Message> {
        TODO("not implemented")
    }

    override fun execute(setChatPhoto: SetChatPhoto?): Boolean {
        TODO("not implemented")
    }

    override fun execute(addStickerToSet: AddStickerToSet?): Boolean {
        TODO("not implemented")
    }

    override fun execute(setStickerSetThumb: SetStickerSetThumb?): Boolean {
        TODO("not implemented")
    }

    override fun execute(createNewStickerSet: CreateNewStickerSet?): Boolean {
        TODO("not implemented")
    }

    override fun execute(uploadStickerFile: UploadStickerFile?): File {
        TODO("not implemented")
    }

    override fun execute(editMessageMedia: EditMessageMedia?): Serializable {
        TODO("not implemented")
    }

    override fun execute(sendAnimation: SendAnimation?): Message {
        TODO("not implemented")
    }
}
