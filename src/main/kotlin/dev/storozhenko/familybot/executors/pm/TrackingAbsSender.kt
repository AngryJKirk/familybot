package dev.storozhenko.familybot.executors.pm

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
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
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.updateshandlers.SentCallback
import java.io.Serializable
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

@Suppress("UNCHECKED_CAST")
class TrackingAbsSender(private val absSender: AbsSender) : AbsSender() {

    override fun <T : Serializable?, Method : BotApiMethod<T>?, Callback : SentCallback<T>?> executeAsync(
        method: Method,
        callback: Callback
    ) {
        absSender.executeAsync(method, callback)
    }

    override fun <T : Serializable?, Method : BotApiMethod<T>?> executeAsync(method: Method): CompletableFuture<T> {
        return absSender.executeAsync(method)
    }

    override fun <T : Serializable?, Method : BotApiMethod<T>?> execute(method: Method): T {
        val execute = absSender.execute(method)
        if (execute is Message) {
            tracking.add(execute as Message)
        }
        return execute
    }

    override fun execute(sendDocument: SendDocument): Message {
        val message = absSender.execute(sendDocument)
        tracking.add(message)
        return message
    }

    override fun execute(sendPhoto: SendPhoto): Message {
        val message = absSender.execute(sendPhoto)
        tracking.add(message)
        return message
    }

    override fun execute(sendVideo: SendVideo): Message {
        val message = absSender.execute(sendVideo)
        tracking.add(message)
        return message
    }

    override fun execute(sendVideoNote: SendVideoNote): Message {
        val message = absSender.execute(sendVideoNote)
        tracking.add(message)
        return message
    }

    override fun execute(sendSticker: SendSticker): Message {
        val message = absSender.execute(sendSticker)
        tracking.add(message)
        return message
    }

    override fun execute(sendAudio: SendAudio): Message {
        val message = absSender.execute(sendAudio)
        tracking.add(message)
        return message
    }

    override fun execute(sendVoice: SendVoice): Message {
        val message = absSender.execute(sendVoice)
        tracking.add(message)
        return message
    }

    override fun execute(sendMediaGroup: SendMediaGroup): List<Message> {
        return absSender.execute(sendMediaGroup)
    }

    override fun execute(setChatPhoto: SetChatPhoto): Boolean {
        return absSender.execute(setChatPhoto)
    }

    override fun execute(addStickerToSet: AddStickerToSet): Boolean {
        return absSender.execute(addStickerToSet)
    }

    override fun execute(setStickerSetThumb: SetStickerSetThumb): Boolean {
        return absSender.execute(setStickerSetThumb)
    }

    override fun execute(createNewStickerSet: CreateNewStickerSet): Boolean {
        return absSender.execute(createNewStickerSet)
    }

    override fun execute(uploadStickerFile: UploadStickerFile): File {
        return absSender.execute(uploadStickerFile)
    }

    override fun execute(editMessageMedia: EditMessageMedia): Serializable {
        return absSender.execute(editMessageMedia)
    }

    override fun execute(sendAnimation: SendAnimation): Message {
        val message = absSender.execute(sendAnimation)
        tracking.add(message)
        return message
    }

    override fun executeAsync(sendDocument: SendDocument): CompletableFuture<Message> {
        return absSender.executeAsync(sendDocument)
    }

    override fun executeAsync(sendPhoto: SendPhoto): CompletableFuture<Message> {
        return absSender.executeAsync(sendPhoto)
    }

    override fun executeAsync(sendVideo: SendVideo): CompletableFuture<Message> {
        return absSender.executeAsync(sendVideo)
    }

    override fun executeAsync(sendVideoNote: SendVideoNote): CompletableFuture<Message> {
        return absSender.executeAsync(sendVideoNote)
    }

    override fun executeAsync(sendSticker: SendSticker): CompletableFuture<Message> {
        return absSender.executeAsync(sendSticker)
    }

    override fun executeAsync(sendAudio: SendAudio): CompletableFuture<Message> {
        return absSender.executeAsync(sendAudio)
    }

    override fun executeAsync(sendVoice: SendVoice): CompletableFuture<Message> {
        return absSender.executeAsync(sendVoice)
    }

    override fun executeAsync(sendMediaGroup: SendMediaGroup): CompletableFuture<List<Message>> {
        return absSender.executeAsync(sendMediaGroup)
    }

    override fun executeAsync(setChatPhoto: SetChatPhoto): CompletableFuture<Boolean> {
        return absSender.executeAsync(setChatPhoto)
    }

    override fun executeAsync(addStickerToSet: AddStickerToSet): CompletableFuture<Boolean> {
        return absSender.executeAsync(addStickerToSet)
    }

    override fun executeAsync(setStickerSetThumb: SetStickerSetThumb): CompletableFuture<Boolean> {
        return absSender.executeAsync(setStickerSetThumb)
    }

    override fun executeAsync(createNewStickerSet: CreateNewStickerSet): CompletableFuture<Boolean> {
        return absSender.executeAsync(createNewStickerSet)
    }

    override fun executeAsync(uploadStickerFile: UploadStickerFile): CompletableFuture<File> {
        return absSender.executeAsync(uploadStickerFile)
    }

    override fun executeAsync(editMessageMedia: EditMessageMedia): CompletableFuture<Serializable> {
        return absSender.executeAsync(editMessageMedia)
    }

    override fun executeAsync(sendAnimation: SendAnimation): CompletableFuture<Message> {
        return absSender.executeAsync(sendAnimation)
    }

    public override fun <T : Serializable?, Method : BotApiMethod<T>?, Callback : SentCallback<T>?> sendApiMethodAsync(
        method: Method,
        callback: Callback
    ) {
        callProtectedMethod(absSender, "sendApiMethodAsync", method, callback)
    }

    public override fun <T : Serializable?, Method : BotApiMethod<T>?> sendApiMethodAsync(method: Method): CompletableFuture<T> {
        return callProtectedMethod(absSender, "sendApiMethodAsync", method) as CompletableFuture<T>
    }

    public override fun <T : Serializable?, Method : BotApiMethod<T>?> sendApiMethod(method: Method): T {
        return callProtectedMethod(absSender, "sendApiMethod", method) as T
    }

    val tracking: MutableList<Message> = ArrayList()

    companion object {
        fun callProtectedMethod(obj: Any, methodName: String, vararg args: Any?): Any? {
            val argsArray = args.map { it?.javaClass ?: Unit::class.java }.toTypedArray()
            val method = obj.javaClass.getDeclaredMethod(methodName, *argsArray)
            val previousState = method.canAccess(obj)
            method.isAccessible = true
            return method.invoke(obj, *args).also { method.isAccessible = previousState }
        }
    }
}
