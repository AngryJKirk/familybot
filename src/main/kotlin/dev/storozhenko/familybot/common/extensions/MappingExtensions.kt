package dev.storozhenko.familybot.common.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
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
    val message = message()
    return Chat(message.chat.id, message.chat.title)
}

fun Update.chatId(): Long = message().chatId

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

fun Update.message(): Message {
    val callbackMessage = callbackQuery?.message
    return when {
        message != null -> message
        editedMessage != null -> editedMessage
        callbackMessage != null && callbackMessage is Message -> callbackMessage
        else -> throw FamilyBot.InternalException("Message is not available for $this")
    }
}

fun Update.context(botConfig: BotConfig, dictionary: Dictionary, sender: AbsSender): ExecutorContext {
    val message = message()
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
        sender,
        dictionary,
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

fun Any.toJson(pretty: Boolean = false): String {
    return if (pretty) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    } else {
        objectMapper.writeValueAsString(this)
    }
}

inline fun <reified T> String.parseJson(): T = mapper().readValue(this, T::class.java)
