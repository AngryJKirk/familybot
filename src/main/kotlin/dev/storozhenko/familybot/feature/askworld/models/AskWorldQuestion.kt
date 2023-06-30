package dev.storozhenko.familybot.feature.askworld.models

import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import java.time.Instant

data class AskWorldQuestion(
    val id: Long?,
    val message: String,
    val user: User,
    val chat: Chat,
    val date: Instant,
    val messageId: Long?,
)
