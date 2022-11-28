package space.yaroslav.familybot.models.askworld

import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
import java.time.Instant

data class AskWorldReply(
    val id: Long?,
    val questionId: Long,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant
)
