package space.yaroslav.familybot.common.utils

import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.Chat as TelegramChat
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

fun TelegramChat.toChat(): Chat = Chat(this.id, this.title)

fun TelegramUser.toUser(chat: Chat? = null, telegramChat: TelegramChat? = null): User {
    val internalChat = telegramChat?.toChat()
        ?: chat
        ?: throw FamilyBot.InternalException("Should be some chat to map user to internal model")

    val format = (this.firstName?.let { "$it " } ?: "") + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat, format, this.userName)
}

fun Update.toChat(): Chat {
    return when {
        this.hasMessage() -> Chat(message.chat.id, message.chat.title)
        this.hasEditedMessage() -> Chat(editedMessage.chat.id, editedMessage.chat.title)
        else -> Chat(this.callbackQuery.message.chat.id, this.callbackQuery.message.chat.title)
    }
}

fun Update.chatId(): Long {
    return when {
        this.hasMessage() -> message.chat.id
        this.hasEditedMessage() -> editedMessage.chat.id
        else -> callbackQuery.message.chat.id
    }
}

fun Update.toUser(): User {
    val user = this.from()
    val formatedName = (user.firstName?.let { "$it " } ?: "") + (user.lastName ?: "")
    return User(user.id.toLong(), this.toChat(), formatedName, user.userName)
}

fun Update.from(): TelegramUser {
    return when {
        this.hasMessage() -> this.message.from
        this.hasEditedMessage() -> this.editedMessage.from
        this.hasCallbackQuery() -> this.callbackQuery.from
        else -> throw RuntimeException("Cant process ${this}")
    }
}
