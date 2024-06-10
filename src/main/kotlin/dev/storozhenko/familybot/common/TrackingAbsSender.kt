package dev.storozhenko.familybot.common

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
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
import org.telegram.telegrambots.meta.api.methods.stickers.ReplaceStickerInSet
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumbnail
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.InputStream
import java.io.Serializable
import java.util.concurrent.CompletableFuture

class TrackingTelegramClient(
    private val telegramClient: TelegramClient,
    val tracking: MutableList<Message> = ArrayList(),
) : TelegramClient {

    override fun <T : Serializable?, Method : BotApiMethod<T>?> executeAsync(method: Method): CompletableFuture<T> {
        return telegramClient.executeAsync(method)
    }

    override fun <T : Serializable?, Method : BotApiMethod<T>?> execute(method: Method): T {
        val execute = telegramClient.execute(method)
        if (execute is Message) {
            tracking.add(execute)
        }
        return execute
    }

    override fun execute(sendDocument: SendDocument): Message {
        val message = telegramClient.execute(sendDocument)
        tracking.add(message)
        return message
    }

    override fun execute(sendPhoto: SendPhoto): Message {
        val message = telegramClient.execute(sendPhoto)
        tracking.add(message)
        return message
    }

    override fun execute(sendVideo: SendVideo): Message {
        val message = telegramClient.execute(sendVideo)
        tracking.add(message)
        return message
    }

    override fun execute(sendVideoNote: SendVideoNote): Message {
        val message = telegramClient.execute(sendVideoNote)
        tracking.add(message)
        return message
    }

    override fun execute(sendSticker: SendSticker): Message {
        val message = telegramClient.execute(sendSticker)
        tracking.add(message)
        return message
    }

    override fun execute(sendAudio: SendAudio): Message {
        val message = telegramClient.execute(sendAudio)
        tracking.add(message)
        return message
    }

    override fun execute(sendVoice: SendVoice): Message {
        val message = telegramClient.execute(sendVoice)
        tracking.add(message)
        return message
    }

    override fun execute(sendMediaGroup: SendMediaGroup): List<Message> {
        return telegramClient.execute(sendMediaGroup)
    }

    override fun execute(setChatPhoto: SetChatPhoto): Boolean {
        return telegramClient.execute(setChatPhoto)
    }

    override fun execute(addStickerToSet: AddStickerToSet): Boolean {
        return telegramClient.execute(addStickerToSet)
    }

    override fun execute(replaceStickerInSet: ReplaceStickerInSet?): Boolean {
        return telegramClient.execute(replaceStickerInSet)
    }

    override fun execute(setStickerSetThumbnail: SetStickerSetThumbnail?): Boolean {
        return telegramClient.execute(setStickerSetThumbnail)
    }

    override fun execute(createNewStickerSet: CreateNewStickerSet): Boolean {
        return telegramClient.execute(createNewStickerSet)
    }

    override fun execute(uploadStickerFile: UploadStickerFile): File {
        return telegramClient.execute(uploadStickerFile)
    }

    override fun execute(editMessageMedia: EditMessageMedia): Serializable {
        return telegramClient.execute(editMessageMedia)
    }

    override fun execute(sendAnimation: SendAnimation): Message {
        val message = telegramClient.execute(sendAnimation)
        tracking.add(message)
        return message
    }

    override fun downloadFile(file: File?): java.io.File {
        return telegramClient.downloadFile(file)
    }

    override fun downloadFileAsStream(file: File?): InputStream {
        return telegramClient.downloadFileAsStream(file)
    }

    override fun downloadFileAsync(file: File?): CompletableFuture<java.io.File> {
        return telegramClient.downloadFileAsync(file)
    }

    override fun downloadFileAsStreamAsync(file: File?): CompletableFuture<InputStream> {
        return telegramClient.downloadFileAsStreamAsync(file)
    }


    override fun executeAsync(sendDocument: SendDocument): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendDocument)
    }

    override fun executeAsync(sendPhoto: SendPhoto): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendPhoto)
    }

    override fun executeAsync(sendVideo: SendVideo): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendVideo)
    }

    override fun executeAsync(sendVideoNote: SendVideoNote): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendVideoNote)
    }

    override fun executeAsync(sendSticker: SendSticker): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendSticker)
    }

    override fun executeAsync(sendAudio: SendAudio): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendAudio)
    }

    override fun executeAsync(sendVoice: SendVoice): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendVoice)
    }

    override fun executeAsync(sendMediaGroup: SendMediaGroup): CompletableFuture<List<Message>> {
        return telegramClient.executeAsync(sendMediaGroup)
    }

    override fun executeAsync(setChatPhoto: SetChatPhoto): CompletableFuture<Boolean> {
        return telegramClient.executeAsync(setChatPhoto)
    }

    override fun executeAsync(addStickerToSet: AddStickerToSet): CompletableFuture<Boolean> {
        return telegramClient.executeAsync(addStickerToSet)
    }

    override fun executeAsync(replaceStickerInSet: ReplaceStickerInSet?): CompletableFuture<Boolean> {
        return telegramClient.executeAsync(replaceStickerInSet)
    }

    override fun executeAsync(setStickerSetThumb: SetStickerSetThumbnail?): CompletableFuture<Boolean> {
        return executeAsync(setStickerSetThumb)
    }

    override fun executeAsync(createNewStickerSet: CreateNewStickerSet): CompletableFuture<Boolean> {
        return telegramClient.executeAsync(createNewStickerSet)
    }

    override fun executeAsync(uploadStickerFile: UploadStickerFile): CompletableFuture<File> {
        return telegramClient.executeAsync(uploadStickerFile)
    }

    override fun executeAsync(editMessageMedia: EditMessageMedia): CompletableFuture<Serializable> {
        return telegramClient.executeAsync(editMessageMedia)
    }

    override fun executeAsync(sendAnimation: SendAnimation): CompletableFuture<Message> {
        return telegramClient.executeAsync(sendAnimation)
    }
}
