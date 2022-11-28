package dev.storozhenko.familybot.models

import dev.storozhenko.familybot.models.telegram.User
import java.time.Instant

data class Marriage(
    val chatId: Long,
    val firstUser: User,
    val secondUser: User,
    val startDate: Instant = Instant.now()
)
