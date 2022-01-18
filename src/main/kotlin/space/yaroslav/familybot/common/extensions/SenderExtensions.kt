package space.yaroslav.familybot.common.extensions

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
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.models.telegram.stickers.StickerPack
import space.yaroslav.familybot.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

object SenderLogger {
    val log = getLogger()
}

suspend fun AbsSender.sendDeferred(
    context: ExecutorContext,
    text: Deferred<String>,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Int, Int> = 1000 to 2000
): Message {
    return sendInternal(
        context,
        { text.await() },
        replyMessageId,
        enableHtml,
        replyToUpdate,
        customization,
        shouldTypeBeforeSend,
        typeDelay
    )
}


suspend fun AbsSender.send(
    context: ExecutorContext,
    text: String,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Int, Int> = 1000 to 2000
): Message {
    return sendInternal(
        context,
        { text },
        replyMessageId,
        enableHtml,
        replyToUpdate,
        customization,
        shouldTypeBeforeSend,
        typeDelay
    )
}

private suspend fun AbsSender.sendInternal(
    context: ExecutorContext,
    text: suspend () -> String,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Int, Int> = 1000 to 2000
): Message {
    val update = context.update
    SenderLogger.log.info(
        "Sending message, update=${update.toJson()}, " +
                "replyMessageId=$replyMessageId," +
                "enableHtml=$enableHtml," +
                "replyToUpdate=$replyToUpdate," +
                "shouldTypeBeforeSend=$shouldTypeBeforeSend," +
                "typeDelay=$typeDelay"
    )
    if (shouldTypeBeforeSend) {
        this.execute(SendChatAction(update.chatIdString(), "typing"))
        if (context.testEnvironment.not()) {
            delay(randomInt(typeDelay.first, typeDelay.second).toLong())
        }
    }
    val textToSend = text()
    SenderLogger.log.info("Sending message, text=$textToSend")
    val messageObj = SendMessage(update.chatIdString(), textToSend).apply { enableHtml(enableHtml) }

    if (replyMessageId != null) {
        messageObj.replyToMessageId = replyMessageId
    }
    if (replyToUpdate) {
        messageObj.replyToMessageId = update.message.messageId
    }


    val message = messageObj.apply(customization)

    SenderLogger.log.info("Sending message: ${message.toJson()}")

    return this.execute(message)
}

suspend fun AbsSender.sendSticker(
    context: ExecutorContext,
    sticker: Sticker,
    replyToUpdate: Boolean = false
): Message {
    return sendStickerInternal(this, context, replyToUpdate, sticker.pack) {
        find { it.emoji == sticker.stickerEmoji }
    }
}

suspend fun AbsSender.sendRandomSticker(
    context: ExecutorContext,
    stickerPack: StickerPack,
    replyToUpdate: Boolean = false
): Message {
    return sendStickerInternal(this, context, replyToUpdate, stickerPack) {
        random()
    }
}

fun AbsSender.isFromAdmin(context: ExecutorContext): Boolean {
    if (context.isFromDeveloper) {
        return true
    }
    val user = context.update.from()
    return this
        .execute(GetChatAdministrators(context.chat.idString))
        .filter { chatMember -> chatMember.status == "administrator" || chatMember.status == "creator" }
        .any { admin -> admin.user().id == user.id }
}

private suspend fun sendStickerInternal(
    sender: AbsSender,
    context: ExecutorContext,
    replyToUpdate: Boolean = false,
    stickerPack: StickerPack,
    stickerSelector: List<TelegramSticker>.() -> TelegramSticker?
): Message {

    val stickerId = coroutineScope {
        async {
            stickerSelector(sender.execute(GetStickerSet(stickerPack.packName)).stickers)
        }
    }
    val sendSticker = SendSticker().apply {
        sticker = InputFile(stickerId.await()?.fileId)
        chatId = context.update.chatIdString()
    }
    if (replyToUpdate) {
        sendSticker.replyToMessageId = context.update.message.messageId
    }
    return sender.execute(sendSticker)
}

private val chatMemberAdministrator = ChatMemberAdministrator()
private val chatMemberBanned = ChatMemberBanned()
private val chatMemberLeft = ChatMemberLeft()
private val chatMemberMember = ChatMemberMember()
private val chatMemberOwner = ChatMemberOwner()
private val chatMemberRestricted = ChatMemberRestricted()

fun ChatMember.user(): User {
    return when (status) {
        chatMemberAdministrator.status -> (this as ChatMemberAdministrator).user
        chatMemberBanned.status -> (this as ChatMemberBanned).user
        chatMemberLeft.status -> (this as ChatMemberLeft).user
        chatMemberMember.status -> (this as ChatMemberMember).user
        chatMemberOwner.status -> (this as ChatMemberOwner).user
        chatMemberRestricted.status -> (this as ChatMemberRestricted).user
        else -> throw FamilyBot.InternalException("Can't find mapping for user $this ")
    }
}
