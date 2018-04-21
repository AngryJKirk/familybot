package space.yaroslav.familybot.common.utils


import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import org.telegram.telegrambots.api.objects.Chat as TelegramChat
import org.telegram.telegrambots.api.objects.User as TelegramUser

fun TelegramChat.toChat(): Chat = Chat(this.id, this.title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    val format = (this.firstName?.let { "$it " } ?: "") + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat!!, format, this.userName)
}

fun Update.toChat(): Chat {
    val message = this.toMessage()
    return Chat(message.chat.id, message.chat.title)
}

fun Update.toUser(): User {
    val user = this.toMessage().from
    val formatedName = (user.firstName?.let { "$it " } ?: "") + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formatedName, user.userName)
}

fun Update.toMessage(): Message {
    return when {
        this.hasMessage() -> this.message
        this.hasCallbackQuery() -> this.callbackQuery.message
        this.hasEditedMessage() -> this.editedMessage
        else -> throw RuntimeException("Cant process ${this}")
    }
}