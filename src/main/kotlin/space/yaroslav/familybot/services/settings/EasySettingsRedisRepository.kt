package space.yaroslav.familybot.services.settings

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Duration

@Component
class EasySettingsRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {

    fun <T : Any> put(
        easySetting: EasySetting<T>,
        key: SettingsKey,
        value: T,
        duration: Duration? = null
    ) {
        val keyValue = getKeyValue(easySetting, key)
        if (duration == null) {
            redisTemplate.opsForValue().set(keyValue, value.toString())
        } else {
            redisTemplate.opsForValue().set(keyValue, value.toString(), duration)
        }
    }

    fun <T : Any> get(easySetting: EasySetting<T>, key: SettingsKey): T? {
        val rawValue = redisTemplate.opsForValue().get(getKeyValue(easySetting, key))
            ?: return null

        return cast(easySetting, rawValue)
    }

    fun decrement(easySetting: EasySetting<Long>, key: SettingsKey): Long {
        return redisTemplate.opsForValue().decrement(getKeyValue(easySetting, key)) ?: 0
    }

    fun increment(easySetting: EasySetting<Long>, key: SettingsKey): Long {
        return redisTemplate.opsForValue().increment(getKeyValue(easySetting, key)) ?: 0
    }

    private fun <T : Any> getKeyValue(easySetting: EasySetting<T>, key: SettingsKey): String {
        return "${easySetting.getName()}:${key.chatId}:${key.userId}"
    }

    private fun <T : Any> cast(easySetting: EasySetting<T>, rawValue: String): T {
        val result = when (easySetting.getType()) {
            Boolean::class -> rawValue.toBoolean()
            Long::class -> rawValue.toLong()
            String::class -> rawValue
            else -> throw FamilyBot.InternalException("Parsing for type ${easySetting.getType()} is not implemented")
        }
        return result as T
    }
}

data class SettingsKey(
    val chatId: Long? = null,
    val userId: Long? = null
)
