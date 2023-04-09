package dev.storozhenko.familybot.feature.pidor.models

import dev.storozhenko.familybot.core.models.telegram.User
import java.time.Instant

data class Pidor(val user: User, val date: Instant)
