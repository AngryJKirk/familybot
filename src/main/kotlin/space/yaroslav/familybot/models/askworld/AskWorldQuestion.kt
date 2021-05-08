package space.yaroslav.familybot.models.askworld

import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
import java.time.Instant

data class AskWorldQuestion(
    val id: Long?,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant,
    val messageId: Long?
)