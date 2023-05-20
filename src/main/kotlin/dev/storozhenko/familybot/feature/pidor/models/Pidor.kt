package dev.storozhenko.familybot.feature.pidor.models

import com.fasterxml.jackson.annotation.JsonProperty
import dev.storozhenko.familybot.core.models.telegram.User
import java.time.Instant

data class Pidor(val user: User, val date: Instant)

data class PidorStrikes(
    @JsonProperty("stats") val stats: Map<Long, PidorStrikeStat> = mutableMapOf()
)


data class PidorStrikeStat(
    @JsonProperty("currentStrike") val currentStrike: Int,
    @JsonProperty("maxStrike") val maxStrike: Int
)
