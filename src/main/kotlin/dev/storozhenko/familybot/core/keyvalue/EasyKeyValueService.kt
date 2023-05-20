package dev.storozhenko.familybot.core.keyvalue

import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKeyType
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import org.springframework.data.redis.core.ScanOptions.scanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import kotlin.time.toJavaDuration

@Component
class EasyKeyValueService(
    private val redisTemplate: StringRedisTemplate
) {

    fun <INPUT : Any, KEY : EasyKey> put(
        easyKeyType: EasyKeyType<INPUT, KEY>,
        key: KEY,
        value: INPUT,
        duration: kotlin.time.Duration? = null
    ) {
        val keyValue = getKeyValue(easyKeyType, key)
        val stringValue = easyKeyType.mapToString(value)
        if (duration == null) {
            redisTemplate.opsForValue().set(keyValue, stringValue)
        } else {
            redisTemplate.opsForValue().set(keyValue, stringValue, duration.toJavaDuration())
        }
    }

    fun <INPUT : Any, KEY : EasyKey> get(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY, defaultValue: INPUT): INPUT {
        return get(easyKeyType, key) ?: defaultValue
    }

    fun <INPUT : Any, KEY : EasyKey> get(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): INPUT? {
        val rawValue = redisTemplate.opsForValue().get(getKeyValue(easyKeyType, key))
            ?: return null

        return easyKeyType.mapFromString(rawValue)
    }

    fun <INPUT : Any, KEY : EasyKey> getAndRemove(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): INPUT? {
        val rawValue = redisTemplate.opsForValue().getAndDelete(getKeyValue(easyKeyType, key))
            ?: return null

        return easyKeyType.mapFromString(rawValue)
    }

    fun <KEY : EasyKey> decrement(easyKeyType: EasyKeyType<Long, KEY>, key: KEY): Long {
        return redisTemplate.opsForValue().decrement(getKeyValue(easyKeyType, key)) ?: 0
    }

    fun <KEY : EasyKey> increment(easyKeyType: EasyKeyType<Long, KEY>, key: KEY): Long {
        return redisTemplate.opsForValue().increment(getKeyValue(easyKeyType, key)) ?: 0
    }

    fun <INPUT : Any, KEY : EasyKey> remove(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY) {
        redisTemplate.delete(getKeyValue(easyKeyType, key))
    }

    @Suppress("UNCHECKED_CAST")
    fun <INPUT : Any, KEY : EasyKey> getAllByPartKey(easyKeyType: EasyKeyType<INPUT, KEY>): Map<KEY, INPUT> {
        val keys = mutableMapOf<KEY, INPUT>()
        val scanOptions = scanOptions().match(easyKeyType.getName() + "*").build()
        redisTemplate.scan(scanOptions).use { cursor ->
            cursor.forEach { key ->
                val rawValue = redisTemplate.opsForValue().get(key) ?: return@forEach
                val easyKey = parseEasyKey(key) as KEY
                val easyValue = easyKeyType.mapFromString(rawValue)
                keys[easyKey] = easyValue
            }
        }
        return keys
    }

    private fun <INPUT : Any, KEY : EasyKey> getKeyValue(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): String {
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

}
