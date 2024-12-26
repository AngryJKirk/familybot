package dev.storozhenko.familybot.common.extensions

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted
import org.telegram.telegrambots.meta.generics.TelegramClient

object SenderLogger {
    val log = KotlinLogging.logger { }
}

suspend fun TelegramClient.sendContextFree(
    chatId: String,
    text: String,
    botConfig: BotConfig,
    replyMessageId: Int? = null,
    enableHtml: Boolean = false,
    replyToUpdate: Boolean = false,
    customization: SendMessage.() -> Unit = { },
    shouldTypeBeforeSend: Boolean = false,
    typeDelay: Pair<Int, Int> = 1000 to 2000,
) {
    ExecutorContext.sendInternal(
        this,
        chatId,
        botConfig.testEnvironment,
        null,
        null,
        { text },
        replyMessageId,
        enableHtml,
        replyToUpdate,
        customization,
        shouldTypeBeforeSend,
        typeDelay,
    )
}

fun ChatMember.user(): User {
    return when (status) {
        ChatMemberAdministrator.STATUS -> (this as ChatMemberAdministrator).user
        ChatMemberBanned.STATUS -> (this as ChatMemberBanned).user
        ChatMemberLeft.STATUS -> (this as ChatMemberLeft).user
        ChatMemberMember.STATUS -> (this as ChatMemberMember).user
        ChatMemberOwner.STATUS -> (this as ChatMemberOwner).user
        ChatMemberRestricted.STATUS -> (this as ChatMemberRestricted).user
        else -> throw FamilyBot.InternalException("Can't find mapping for user $this ")
    }
}
