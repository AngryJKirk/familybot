package space.yaroslav.familybot.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom


fun org.telegram.telegrambots.api.objects.Chat.toChat(): Chat = Chat(this.id, this.firstName + " " + this.lastName)

fun org.telegram.telegrambots.api.objects.User.toUser(chat: Chat? = null, telegramChat: org.telegram.telegrambots.api.objects.Chat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    val format = this.firstName ?: "" + " " + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat!!, format, this.userName)
}

fun LocalDateTime.isToday(): Boolean {
    return LocalDate.now().atTime(0, 0).isBefore(this)
}

fun <T> Iterable<T>.random(): T {
    val all = ArrayList<T>()
    iterator().forEach { all.add(it) }
    val nextInt = ThreadLocalRandom.current().nextInt(0, all.size)
    return all[nextInt]
}
