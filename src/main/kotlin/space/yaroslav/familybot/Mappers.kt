package space.yaroslav.familybot

import java.time.LocalDate
import java.time.LocalDateTime


fun org.telegram.telegrambots.api.objects.Chat.toChat(): Chat = Chat(this.id, this.firstName + " " + this.lastName)

fun org.telegram.telegrambots.api.objects.User.toUser(chat: Chat? = null, telegramChat: org.telegram.telegrambots.api.objects.Chat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    return User(this.id.toLong(), internalChat!!, this.firstName + " " + this.lastName, this.userName?: this.firstName + " " + this.lastName)
}

fun LocalDateTime.isToday(): Boolean {
    return LocalDate.now().atTime(0, 0).isBefore(this)
}
