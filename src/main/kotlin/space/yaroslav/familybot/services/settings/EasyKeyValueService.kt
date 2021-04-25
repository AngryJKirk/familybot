package space.yaroslav.familybot.services.settings

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EasyKeyValueService(private val easySettingRepository: EasyKeyValueRepository) {

    fun <T : Any, K : EasyKey> put(
        easyKeyType: EasyKeyType<T, K>,
        key: K,
        value: T,
        duration: Duration? = null
    ) {
        easySettingRepository.put(easyKeyType, key, value, duration)
    }

    fun <T : Any, K : EasyKey> get(easyKeyType: EasyKeyType<T, K>, key: K): T? {
        return easySettingRepository.get(easyKeyType, key)
    }

    fun <T : Any, K : EasyKey> get(easyKeyType: EasyKeyType<T, K>, key: K, defaultValue: T): T {
        return easySettingRepository.get(easyKeyType, key) ?: defaultValue
    }

    fun <K : EasyKey> decrement(easyKeyType: EasyKeyType<Long, K>, key: K): Long {
        return easySettingRepository.decrement(easyKeyType, key)
    }

    fun <K : EasyKey> increment(easyKeyType: EasyKeyType<Long, K>, key: K): Long {
        return easySettingRepository.increment(easyKeyType, key)
    }

    fun <T : Any, K : EasyKey> remove(easyKeyType: EasyKeyType<T, K>, key: K) {
        return easySettingRepository.remove(easyKeyType, key)
    }
}
