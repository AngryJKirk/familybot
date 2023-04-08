package dev.storozhenko.familybot.services.settings

import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.data.redis.core.ScanOptions.scanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import kotlin.time.toJavaDuration

@Component
class EasyKeyValueService(
    private val redisTemplate: StringRedisTemplate
) {

    fun <T : Any, K : EasyKey> put(
        easyKeyType: EasyKeyType<T, K>,
        key: K,
        value: T,
        duration: kotlin.time.Duration? = null
    ) {
        val keyValue = getKeyValue(easyKeyType, key)
        if (duration == null) {
            redisTemplate.opsForValue().set(keyValue, value.toString())
        } else {
            redisTemplate.opsForValue().set(keyValue, value.toString(), duration.toJavaDuration())
        }
    }

    fun <T : Any, K : EasyKey> get(easyKeyType: EasyKeyType<T, K>, key: K, defaultValue: T): T {
        return get(easyKeyType, key) ?: defaultValue
    }

    fun <T : Any, K : EasyKey> get(easyKeyType: EasyKeyType<T, K>, key: K): T? {
        val rawValue = redisTemplate.opsForValue().get(getKeyValue(easyKeyType, key))
            ?: return null

        return cast(easyKeyType, rawValue)
    }

    fun <K : EasyKey> decrement(easyKeyType: EasyKeyType<Long, K>, key: K): Long {
        return redisTemplate.opsForValue().decrement(getKeyValue(easyKeyType, key)) ?: 0
    }

    fun <K : EasyKey> increment(easyKeyType: EasyKeyType<Long, K>, key: K): Long {
        return redisTemplate.opsForValue().increment(getKeyValue(easyKeyType, key)) ?: 0
    }

    fun <T : Any, K : EasyKey> remove(easyKeyType: EasyKeyType<T, K>, key: K) {
        redisTemplate.delete(getKeyValue(easyKeyType, key))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any, K : EasyKey> getAllByPartKey(easyKeyType: EasyKeyType<T, K>): Map<K, T> {
        val keys = mutableMapOf<K, T>()
        val scanOptions = scanOptions().match(easyKeyType.getName() + "*").build()
        redisTemplate.scan(scanOptions).use { cursor ->
            cursor.forEach { key ->
                val rawValue = redisTemplate.opsForValue().get(key) ?: return@forEach
                val easyKey = parseEasyKey(key) as K
                val easyValue = cast(easyKeyType, rawValue)
                keys[easyKey] = easyValue
            }
        }
        return keys
    }

    private fun <T : Any, K : EasyKey> getKeyValue(easyKeyType: EasyKeyType<T, K>, key: K): String {
        return "${easyKeyType.getName()}:${key.value()}"
    }

    private fun parseEasyKey(rawValue: String): EasyKey {
        if (rawValue.contains(PlainKey.PREFIX)) {
            val (_, _, key) = rawValue.split(":")
            return PlainKey(key)
        }
        val rawSplit = rawValue.split(":")
        if (rawSplit.size != 3) {
            throw IllegalArgumentException("Wrong key format")
        }
        val (_, chatId, userId) = rawSplit
        if (chatId == "null") {
            return UserEasyKey(userId.toLong())
        }
        if (userId == "null") {
            return ChatEasyKey(chatId.toLong())
        }
        return UserAndChatEasyKey(chatId.toLong(), userId.toLong())
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any, K : EasyKey> cast(easyKeyType: EasyKeyType<T, K>, rawValue: String): T {
        val result = when (easyKeyType.getType()) {
            Boolean::class -> rawValue.toBoolean()
            Long::class -> rawValue.toLong()
            String::class -> rawValue
            else -> throw FamilyBot.InternalException("Parsing for type ${easyKeyType.getType()} is not implemented")
        }
        return result as T
    }
}
