package dev.storozhenko.familybot.core.routers.models

import dev.storozhenko.familybot.common.extensions.SenderLogger
import dev.storozhenko.familybot.common.extensions.chatIdString
import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.common.extensions.user
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.models.telegram.stickers.Sticker
import dev.storozhenko.familybot.core.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

data class ExecutorContext(
    val update: Update,
    val message: Message,
    val command: Command?,
    val isFromDeveloper: Boolean,
    val chat: Chat,
    val user: User,
    val userAndChatKey: UserAndChatEasyKey,
    val userKey: UserEasyKey,
    val chatKey: ChatEasyKey,
    val testEnvironment: Boolean,
    val client: TelegramClient,
    private val dictionary: Dictionary,
) {
    fun phrase(phrase: Phrase) = dictionary.get(phrase, chatKey)
    fun allPhrases(phrase: Phrase) = dictionary.getAll(phrase)

    companion object {
        suspend fun sendInternal(
            client: TelegramClient,
            chatId: String,
            testEnvironment: Boolean,
            messageId: Int? = null,
            update: Update? = null,
            text: suspend () -> String,
            replyMessageId: Int? = null,
            enableHtml: Boolean = false,
            replyToUpdate: Boolean = false,
            customization: SendMessage.() -> Unit = { },
            shouldTypeBeforeSend: Boolean = false,
            typeDelay: Pair<Int, Int> = 1000 to 2000,
            keyboard: (KeyboardDsl.() -> InlineKeyboardMarkup)? = null
        ): Message {
            SenderLogger.log.info {
                "Sending message, update=${update?.toJson() ?: "[N/A]"}, " +
                        "replyMessageId=$replyMessageId," +
                        "enableHtml=$enableHtml," +
                        "replyToUpdate=$replyToUpdate," +
                        "shouldTypeBeforeSend=$shouldTypeBeforeSend," +
                        "typeDelay=$typeDelay"
            }
            if (shouldTypeBeforeSend) {
                client.execute(SendChatAction(chatId, "typing"))
                if (testEnvironment.not()) {
                    delay(randomInt(typeDelay.first, typeDelay.second).toLong())
                }
            }
            val textToSend = text()
            SenderLogger.log.info { "Sending message, text=$textToSend" }
            return textToSend
                .chunked(3900)
                .map {
                    SendMessage(chatId, textToSend)
                        .apply {
                            enableHtml(enableHtml)
                            if (replyMessageId != null) {
                                replyToMessageId = replyMessageId
                            }
                            if (replyToUpdate) {
                                replyToMessageId = messageId
                            }
                            replyMarkup = keyboard?.invoke(KeyboardDsl())
                            customization()
                        }
                }.map { message ->
                    SenderLogger.log.info { "Sending message: ${message.toJson()}" }
                    client.execute(message)
                }.first()
        }

    }

    suspend fun sendDeferred(
        text: Deferred<String>,
        replyMessageId: Int? = null,
        enableHtml: Boolean = false,
        replyToUpdate: Boolean = false,
        customization: SendMessage.() -> Unit = { },
        shouldTypeBeforeSend: Boolean = false,
        typeDelay: Pair<Int, Int> = 1000 to 2000,
    ): Message {
        return sendInternal(
            client,
            chat.idString,
            testEnvironment,
            update.message?.messageId,
            update,
            { text.await() },
            replyMessageId,
            enableHtml,
            replyToUpdate,
            customization,
            shouldTypeBeforeSend,
            typeDelay,
        )
    }

    suspend fun send(
        text: String,
        replyMessageId: Int? = null,
        enableHtml: Boolean = false,
        replyToUpdate: Boolean = false,
        customization: SendMessage.() -> Unit = { },
        shouldTypeBeforeSend: Boolean = false,
        typeDelay: Pair<Int, Int> = 1000 to 2000,
        keyboard: (KeyboardDsl.() -> InlineKeyboardMarkup)? = null
    ): Message {
        return sendInternal(
            client,
            chat.idString,
            testEnvironment,
            update.message?.messageId,
            update,
            { text },
            replyMessageId,
            enableHtml,
            replyToUpdate,
            customization,
            shouldTypeBeforeSend,
            typeDelay,
            keyboard
        )
    }


    suspend fun sendSticker(
        sticker: Sticker,
        replyToUpdate: Boolean = false,
    ): Message {
        return sendStickerInternal(replyToUpdate, sticker.pack) {
            find { it.emoji == sticker.stickerEmoji }
        }
    }

    suspend fun sendRandomSticker(
        stickerPack: StickerPack,
        replyToUpdate: Boolean = false,
    ): Message {
        return sendStickerInternal(replyToUpdate, stickerPack) {
            random()
        }
    }

    private val adminStatuses = setOf(ChatMemberAdministrator.STATUS, ChatMemberOwner.STATUS)

    fun isFromAdmin(): Boolean {
        if (isFromDeveloper) {
            return true
        }
        val user = update.from()
        return client.execute(GetChatAdministrators(chat.idString))
            .filter { chatMember -> chatMember.status in adminStatuses }
            .any { admin -> admin.user().id == user.id }
    }

    private suspend fun sendStickerInternal(
        replyToUpdate: Boolean = false,
        stickerPack: StickerPack,
        stickerSelector: List<TelegramSticker>.() -> TelegramSticker?,
    ): Message {
        val stickerId = coroutineScope {
            async {
                stickerSelector(client.execute(GetStickerSet(stickerPack.packName)).stickers)
            }
        }
        val sendSticker = SendSticker(update.chatIdString(), InputFile(stickerId.await()?.fileId))
        if (replyToUpdate) {
            sendSticker.replyToMessageId = update.message.messageId
        }
        return client.execute(sendSticker)
    }
}
