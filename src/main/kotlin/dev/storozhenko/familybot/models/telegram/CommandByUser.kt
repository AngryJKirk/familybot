package dev.storozhenko.familybot.models.telegram

import java.time.Instant

data class CommandByUser(val user: User, val command: Command, val date: Instant)
