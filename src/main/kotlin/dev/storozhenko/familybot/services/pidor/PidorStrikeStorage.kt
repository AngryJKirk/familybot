package dev.storozhenko.familybot.services.pidor

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.PidorStrikeStats

@Component
class PidorStrikeStorage(
    private val easyKeyValueService: EasyKeyValueService
) {

    fun get(chatEasyKey: ChatEasyKey): PidorStrikes {
        val rawValue = easyKeyValueService.get(PidorStrikeStats, chatEasyKey)
        return if (rawValue.isNullOrBlank()) {
            PidorStrikes(mutableMapOf())
        } else {
            rawValue.parseJson()
        }
    }

    fun save(chatEasyKey: ChatEasyKey, strikes: PidorStrikes) {
        easyKeyValueService.put(PidorStrikeStats, chatEasyKey, strikes.toJson())
    }
}

data class PidorStrikes(
    @JsonProperty("stats") val stats: Map<Long, PidorStrikeStat>
)

data class PidorStrikeStat(
    @JsonProperty("currentStrike") val currentStrike: Int,
    @JsonProperty("maxStrike") val maxStrike: Int
)
