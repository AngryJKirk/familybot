package dev.storozhenko.familybot.core.keyvalue

import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKeyType
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import dev.storozhenko.familybot.getLogger
import org.springframework.data.redis.core.ScanOptions.scanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import kotlin.time.toJavaDuration

@Component
class EasyKeyValueService(
    private val redisTemplate: StringRedisTemplate,
) {

    private val log = getLogger()

    fun <INPUT : Any, KEY : EasyKey> put(
        easyKeyType: EasyKeyType<INPUT, KEY>,
        key: KEY,
        value: INPUT,
        duration: kotlin.time.Duration? = null,
    ) {
        val rawKey = getKeyRaw(easyKeyType, key)
        val stringValue = easyKeyType.mapToString(value)
        if (duration == null) {
            redisTemplate.opsForValue().set(rawKey, stringValue)
            log.info("Set $rawKey => $stringValue")
        } else {
            redisTemplate.opsForValue().set(rawKey, stringValue, duration.toJavaDuration())
            log.info("Set $rawKey => $stringValue with duration $duration")
        }
    }

    fun <INPUT : Any, KEY : EasyKey> get(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY, defaultValue: INPUT): INPUT {
        val rawKey = getKeyRaw(easyKeyType, key)

        val value = getInternal(easyKeyType, rawKey)
        if (value != null) {
            log.info("Got $rawKey => $value")
        } else {
            log.info("Got null for $rawKey, default => $defaultValue")
        }
        return value ?: defaultValue
    }

    fun <INPUT : Any, KEY : EasyKey> get(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): INPUT? {
        val rawKey = getKeyRaw(easyKeyType, key)
        val value = getInternal(easyKeyType, rawKey)
        log.info("Got $rawKey => $value")
        return value
    }

    private fun <INPUT : Any, KEY : EasyKey> getInternal(
        easyKeyType: EasyKeyType<INPUT, KEY>,
        rawKey: String
    ): INPUT? {
        val rawValue = redisTemplate.opsForValue().get(rawKey) ?: return null

        return easyKeyType.mapFromString(rawValue)
    }

    fun <INPUT : Any, KEY : EasyKey> getAndRemove(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): INPUT? {
        val rawKey = getKeyRaw(easyKeyType, key)
        val rawValue = redisTemplate.opsForValue().getAndDelete(rawKey)
        log.info("Got $rawKey => $rawValue, deleted after")
        if (rawValue == null) {
            return null
        }
        return easyKeyType.mapFromString(rawValue)
    }

    fun <KEY : EasyKey> decrement(easyKeyType: EasyKeyType<Long, KEY>, key: KEY): Long {
        val rawKey = getKeyRaw(easyKeyType, key)
        val decrementResult = redisTemplate.opsForValue().decrement(rawKey)
        return if (decrementResult != null) {
            log.info("Decremented $rawKey => $decrementResult")
            decrementResult
        } else {
            log.info("Decremented $rawKey => null, returning 0")
            0
        }
    }

    fun <KEY : EasyKey> increment(easyKeyType: EasyKeyType<Long, KEY>, key: KEY): Long {
        val rawKey = getKeyRaw(easyKeyType, key)
        val incrementedResult = redisTemplate.opsForValue().increment(rawKey)
        return if (incrementedResult != null) {
            log.info("Incremented $rawKey => $incrementedResult")
            incrementedResult
        } else {
            log.info("Incremented $rawKey => null, returning 0")
            0
        }
    }

    fun <INPUT : Any, KEY : EasyKey> remove(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY) {
        val rawKey = getKeyRaw(easyKeyType, key)
        val deleted = redisTemplate.delete(rawKey)
        if(deleted) {
            log.info("Deleted $rawKey")
        } else {
            log.info("Attempt to delete non-existing $rawKey")
        }
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
        log.info("Got all by partKey ${easyKeyType.getName()}* => $keys")
        return keys
    }

    private fun <INPUT : Any, KEY : EasyKey> getKeyRaw(easyKeyType: EasyKeyType<INPUT, KEY>, key: KEY): String {
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
