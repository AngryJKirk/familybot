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
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.objects.ChatMember
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.stickers.StickerSet
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.updateshandlers.SentCallback
import java.io.Serializable

class TestSender(val actions: MutableList<Action<*>> = ArrayList()) : AbsSender() {

    override fun <T : Serializable?, Method : BotApiMethod<T>?> sendApiMethod(method: Method): T {
        if(method is GetChatMember){
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
