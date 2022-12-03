package dev.storozhenko.familybot.common.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.UserAndChatEasyKey
import dev.storozhenko.familybot.services.settings.UserEasyKey
import dev.storozhenko.familybot.services.talking.Dictionary
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import org.telegram.telegrambots.meta.api.objects.Chat as TelegramChat
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

fun TelegramChat.toChat(): Chat = Chat(id, title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat()
        ?: chat
        ?: throw FamilyBot.InternalException("Should be some chat to map user to internal model")
    val formattedName = if (lastName != null) {
        "$firstName $lastName"
    } else {
        firstName
    }
    return User(id, internalChat, formattedName, userName)
}

fun Update.toChat(): Chat {
    return when {
        hasMessage() -> Chat(message.chat.id, message.chat.title)
        hasEditedMessage() -> Chat(editedMessage.chat.id, editedMessage.chat.title)
        else -> Chat(callbackQuery.message.chat.id, callbackQuery.message.chat.title)
    }
}

fun Update.chatId(): Long {
    return when {
        hasMessage() -> message.chat.id
        hasEditedMessage() -> editedMessage.chat.id
        else -> callbackQuery.message.chat.id
    }
}

fun Update.chatIdString(): String {
    return chatId().toString()
}

fun Update.toUser(): User {
    val user = from()
    val formattedName = (user.firstName.let { "$it " }) + (user.lastName ?: "")
    return User(user.id, toChat(), formattedName, user.userName)
}

fun Update.from(): TelegramUser {
    return when {
        hasMessage() -> message.from
        hasEditedMessage() -> editedMessage.from
        hasCallbackQuery() -> callbackQuery.from
        hasPollAnswer() -> pollAnswer.user
        hasPreCheckoutQuery() -> preCheckoutQuery.from
        else -> throw FamilyBot.InternalException("Cant process $this")
    }
}

fun Update.context(botConfig: BotConfig, dictionary: Dictionary): ExecutorContext {
    val message = message ?: editedMessage ?: callbackQuery.message
    val isFromDeveloper = botConfig.developer == from().userName
    val chat = toChat()
    val user = toUser()
    return ExecutorContext(
        this,
        message,
        message.getCommand(botConfig.botName),
        isFromDeveloper,
        chat,
        user,
        UserAndChatEasyKey(user.id, chat.id),
        user.key(),
        chat.key(),
        botConfig.testEnvironment,
        dictionary
    )
}

fun Message.getCommand(botName: String): Command? {
    val entities = entities ?: return null
    for (entity in entities) {
        if (entity.offset == 0 && entity.type == EntityType.BOTCOMMAND) {
            val parts = entity.text.split("@")
            if (parts.size == 1) {
                return Command.LOOKUP[parts[0]]
            }
            if (parts[1] == botName) {
                return Command.LOOKUP[parts[0]]
            }
        }
    }
    return null
}

fun Update.getMessageTokens(delimiter: String = " "): List<String> {
    return if (message.hasText()) {
        message.text.split(delimiter)
    } else {
        emptyList()
    }
}

fun Message.key(): UserAndChatEasyKey {
    return UserAndChatEasyKey(from.id, chatId)
}

fun User.key(): UserEasyKey {
    return UserEasyKey(userId = id)
}

fun Chat.key(): ChatEasyKey {
    return ChatEasyKey(chatId = id)
}

fun Month.toRussian(): String {
    return this.getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
}

fun Boolean.toEmoji(): String {
    return if (this) "✅" else "❌"
}

fun Int.rubles() = this * 100

private val objectMapper = jacksonObjectMapper()
fun mapper() = objectMapper

fun Any.toJson(): String = objectMapper.writeValueAsString(this)
inline fun <reified T> String.parseJson(): T = mapper().readValue(this, T::class.java)
