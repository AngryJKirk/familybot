package space.yaroslav.familybot.services.pidor

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.parseJson
import space.yaroslav.familybot.common.extensions.toJson
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorStrikeStats

@Component
class PidorStrikeStorage(
    private val easyKeyValueService: EasyKeyValueService
) {

    fun get(chat: Chat): PidorStrikes {
        val key = chat.key()
        val rawValue = easyKeyValueService.get(PidorStrikeStats, key)
        return if (rawValue.isNullOrBlank()) {
            PidorStrikes(mutableMapOf())
        } else {
            deserialize(rawValue)
        }
    }

    fun save(executorContext: ExecutorContext, strikes: PidorStrikes) {
        easyKeyValueService.put(PidorStrikeStats, executorContext.chat.key(), serialize(strikes))
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
