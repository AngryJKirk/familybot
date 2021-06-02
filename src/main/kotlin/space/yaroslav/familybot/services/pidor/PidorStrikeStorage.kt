package space.yaroslav.familybot.services.pidor

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.toChat
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
        return Json.decodeFromString(raw)
    }

    private fun serialize(pidorStrikes: PidorStrikes): String {
        return Json.encodeToString(pidorStrikes)
    }
}

@Serializable
data class PidorStrikes(
    val stats: Map<Long, PidorStrikeStat>
)

@Serializable
data class PidorStrikeStat(
    val currentStrike: Int,
    val maxStrike: Int
)
