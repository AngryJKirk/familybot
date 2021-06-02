package space.yaroslav.familybot.services.pidor

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.parseJson
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toJson
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorStrikeStats

@Component
class PidorStrikeStorage(
    private val easyKeyValueService: EasyKeyValueService
) {

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
        return raw.parseJson()
    }

    private fun serialize(pidorStrikes: PidorStrikes): String {
        return pidorStrikes.toJson()
    }
}

data class PidorStrikes(
    @JsonProperty("stats") val stats: Map<Long, PidorStrikeStat>
)

data class PidorStrikeStat(
    @JsonProperty("currentStrike") val currentStrike: Int,
    @JsonProperty("maxStrike") val maxStrike: Int
)
