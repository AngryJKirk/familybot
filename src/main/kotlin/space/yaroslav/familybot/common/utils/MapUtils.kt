package space.yaroslav.familybot.common.utils


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
    val message = this.message
            ?: callbackQuery.message
            ?: throw RuntimeException("Cant process ${this}")
    return Chat(message.chat.id, message.chat.title)
}

fun Update.toUser(): User {
    val user = this.message?.from ?: this.callbackQuery.from
    val formatedName = (user.firstName?.let { "$it " } ?: "") + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formatedName, user.userName)
}

fun Update.checkDestinationBot(botName: String?): Boolean {
    val destinationPattern = Regex(pattern = """\/[a-zA-Z]*[@]([a-zA-Z0-9]*).*""")
    val destinationName: String = destinationPattern.matchEntire(this.message.text)?.groups?.get(1)?.value?.toUpperCase() ?: return true
    return botName!!.toUpperCase() == destinationName
}