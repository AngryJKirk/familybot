package space.yaroslav.familybot.common.utils


import org.telegram.telegrambots.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import org.telegram.telegrambots.api.objects.Chat as TelegramChat
import org.telegram.telegrambots.api.objects.User as TelegramUser

fun TelegramChat.toChat(): Chat = Chat(this.id, this.title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    val format = (this.firstName?.let { it + " " } ?: "") + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat!!, format, this.userName)
}

fun Update.toChat(): Chat {
    if (this.message == null) {
        throw RuntimeException("Cant process ${this}")
    }
    return Chat(this.message.chat.id, this.message.chat.title)
}

fun Update.toUser(): User {
    val user = this.message.from
    val formatedName = (user.firstName?.let { it + " " } ?: "") + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formatedName, user.userName)
}