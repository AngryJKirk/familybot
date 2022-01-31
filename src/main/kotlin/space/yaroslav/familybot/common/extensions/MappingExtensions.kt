package space.yaroslav.familybot.common.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.settings.UserEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Month
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
        key(),
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

fun Update.key(): UserAndChatEasyKey {
    return UserAndChatEasyKey(toUser().id, chatId())
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

val monthMap = mapOf(
    Month.JANUARY to "январь",
    Month.FEBRUARY to "февраль",
    Month.MARCH to "март",
    Month.APRIL to "апрель",
    Month.MAY to "май",
    Month.JUNE to "июнь",
    Month.JULY to "июль",
    Month.AUGUST to "август",
    Month.SEPTEMBER to "сентябрь",
    Month.OCTOBER to "октябрь",
    Month.NOVEMBER to "ноябрь",
    Month.DECEMBER to "декабрь"
)

fun Month.toRussian(): String {
    return monthMap.getValue(this)
}

fun Boolean.toEmoji(): String {
    return when (this) {
        true -> "✅"
        false -> "❌"
    }
}

fun Int.rubles() = this * 100

private val objectMapper = jacksonObjectMapper()
fun mapper() = objectMapper

fun Any.toJson(): String = objectMapper.writeValueAsString(this)
inline fun <reified T> String.parseJson(): T = mapper().readValue(this, T::class.java)
