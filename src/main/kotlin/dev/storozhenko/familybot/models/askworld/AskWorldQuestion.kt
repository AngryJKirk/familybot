package dev.storozhenko.familybot.models.askworld

import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.User
import java.time.Instant

data class AskWorldQuestion(
    val id: Long?,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant,
    val messageId: Long?
)
