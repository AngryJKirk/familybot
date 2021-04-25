package space.yaroslav.familybot.services.misc

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorStrikeStats

@Component
class PidorStrikeStorage(
    private val easyKeyValueService: EasyKeyValueService
) {
    private val objectMapper = ObjectMapper()

    fun get(update: Update): PidorStrikes {
        val key = update.toChat().key()
        val rawValue = easyKeyValueService.get(PidorStrikeStats, key)
        return if (rawValue.isNullOrBlank()) {
            PidorStrikes(mutableMapOf())
        } else {
            deserialize(rawValue)
        }
    }

    fun save(update: Update, strikes: PidorStrikes) {
        easyKeyValueService.put(PidorStrikeStats, update.toChat().key(), serialize(strikes))
    }

    private fun deserialize(raw: String): PidorStrikes {
        return objectMapper.readValue(raw, PidorStrikes::class.java)
    }

    private fun serialize(pidorStrikes: PidorStrikes): String {
        return objectMapper.writeValueAsString(pidorStrikes)
    }
}

data class PidorStrikes(
    @JsonProperty("stats") val stats: Map<Long, PidorStrikeStat>
)

data class PidorStrikeStat(
    @JsonProperty("currentStrike") val currentStrike: Int,
    @JsonProperty("maxStrike") val maxStrike: Int
)
