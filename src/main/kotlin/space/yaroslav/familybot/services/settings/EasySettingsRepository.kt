package space.yaroslav.familybot.services.settings

import org.checkerframework.checker.units.qual.K
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Duration

@Component
class EasySettingsRepository(
    private val redisTemplate: StringRedisTemplate
) {

    fun <T : Any, K : SettingsKey> put(
        easySetting: EasySetting<T, K>,
        key: K,
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

    fun <T : Any, K : SettingsKey> get(easySetting: EasySetting<T, K>, key: K): T? {
        val rawValue = redisTemplate.opsForValue().get(getKeyValue(easySetting, key))
            ?: return null

        return cast(easySetting, rawValue)
    }

    fun <K : SettingsKey> decrement(easySetting: EasySetting<Long, K>, key: K): Long {
        return redisTemplate.opsForValue().decrement(getKeyValue(easySetting, key)) ?: 0
    }

    fun <K : SettingsKey> increment(easySetting: EasySetting<Long, K>, key: K): Long {
        return redisTemplate.opsForValue().increment(getKeyValue(easySetting, key)) ?: 0
    }

    fun <T : Any, K : SettingsKey> remove(easySetting: EasySetting<T, K>, key: K) {
        redisTemplate.delete(getKeyValue(easySetting, key))
    }

    private fun <T : Any, K : SettingsKey> getKeyValue(easySetting: EasySetting<T, K>, key: K): String {
        return "${easySetting.getName()}:${key.value()}"
    }

    private fun <T : Any, K : SettingsKey> cast(easySetting: EasySetting<T, K>, rawValue: String): T {
        val result = when (easySetting.getType()) {
            Boolean::class -> rawValue.toBoolean()
            Long::class -> rawValue.toLong()
            String::class -> rawValue
            else -> throw FamilyBot.InternalException("Parsing for type ${easySetting.getType()} is not implemented")
        }
        return result as T
    }
}
