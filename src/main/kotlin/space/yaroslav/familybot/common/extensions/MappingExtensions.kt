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

fun TelegramChat.toChat(): Chat = Chat(this.id, this.title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat()
        ?: chat
        ?: throw FamilyBot.InternalException("Should be some chat to map user to internal model")
    val formattedName = listOfNotNull(this.firstName, this.lastName).joinToString(separator = " ")
    return User(this.id, internalChat, formattedName, this.userName)
}

fun Update.toChat(): Chat {
    return when {
        hasMessage() -> Chat(message.chat.id, message.chat.title)
        hasEditedMessage() -> Chat(editedMessage.chat.id, editedMessage.chat.title)
        else -> Chat(this.callbackQuery.message.chat.id, this.callbackQuery.message.chat.title)
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
    return this.chatId().toString()
}

fun Update.toUser(): User {
    val user = this.from()
    val formattedName = (user.firstName.let { "$it " }) + (user.lastName ?: "")
    return User(user.id, this.toChat(), formattedName, user.userName)
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
    val message = this.message ?: this.editedMessage ?: this.callbackQuery.message
    val isFromDeveloper = botConfig.developer == message.from.userName
    val chat = this.toChat()
    val user = this.toUser()
    return ExecutorContext(
        this,
        message,
        message.getCommand(botConfig.botName),
        isFromDeveloper,
        chat,
        user,
        this.key(),
        user.key(),
        chat.key(),
        dictionary
    )
}

fun Message.getCommand(botName: String): Command? {
    val entities = this.entities ?: return null
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
    return UserAndChatEasyKey(this.toUser().id, this.chatId())
}

fun Message.key(): UserAndChatEasyKey {
    return UserAndChatEasyKey(from.id, chatId)
}

fun User.key(): UserEasyKey {
    return UserEasyKey(userId = this.id)
}

fun Chat.key(): ChatEasyKey {
    return ChatEasyKey(chatId = this.id)
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
