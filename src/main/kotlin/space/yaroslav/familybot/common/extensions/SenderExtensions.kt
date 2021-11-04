package space.yaroslav.familybot.common.extensions

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
import org.telegram.telegrambots.meta.api.objects.Update
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
import space.yaroslav.familybot.models.telegram.stickers.Sticker
import space.yaroslav.familybot.models.telegram.stickers.StickerPack
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker as TelegramSticker

object SenderLogger {
    val log = getLogger()
}

suspend fun AbsSender.send(
    update: Update,
    text: String,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Int, Int> = 1000 to 2000
): Message {
    SenderLogger.log.info(
        "Sending message, update=${update.toJson()}, " +
            "text=$text, " +
            "replyMessageId=$replyMessageId," +
            "enableHtml=$enableHtml," +
            "replyToUpdate=$replyToUpdate," +
            "shouldTypeBeforeSend=$shouldTypeBeforeSend," +
            "typeDelay=$typeDelay"
    )
    val messageObj = SendMessage(update.chatIdString(), text).apply { enableHtml(enableHtml) }

    if (replyMessageId != null) {
        messageObj.replyToMessageId = replyMessageId
    }
    if (replyToUpdate) {
        messageObj.replyToMessageId = update.message.messageId
    }
    if (shouldTypeBeforeSend) {
        this.execute(SendChatAction(update.chatIdString(), "typing"))
        delay(randomInt(typeDelay.first, typeDelay.second).toLong())
    }

    val message = messageObj.apply(customization)

    SenderLogger.log.info("Sending message: ${message.toJson()}")

    return this.execute(message)
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

fun AbsSender.isFromAdmin(update: Update, botConfig: BotConfig): Boolean {
    val user = update.from()
    if (botConfig.developer == user.userName) {
        return true
    }
    return this
        .execute(GetChatAdministrators(update.toChat().idString))
        .filter { chatMember -> chatMember.status == "administrator" || chatMember.status == "creator" }
        .any { admin -> admin.user().id == user.id }
}

private suspend fun sendStickerInternal(
    sender: AbsSender,
    update: Update,
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
        chatId = update.chatIdString()
    }
    if (replyToUpdate) {
        sendSticker.replyToMessageId = update.message.messageId
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
