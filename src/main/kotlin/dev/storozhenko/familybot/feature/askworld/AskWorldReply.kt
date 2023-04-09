package dev.storozhenko.familybot.feature.askworld

import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import java.time.Instant

data class AskWorldReply(
    val id: Long?,
    val questionId: Long,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant
)
