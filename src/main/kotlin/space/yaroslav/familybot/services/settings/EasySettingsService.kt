package space.yaroslav.familybot.services.settings

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EasySettingsService(private val easySettingRepository: EasySettingsRepository) {

    fun <T : Any, K : SettingsKey> put(
        easySetting: EasySetting<T, K>,
        key: K,
        value: T,
        duration: Duration? = null
    ) {
        easySettingRepository.put(easySetting, key, value, duration)
    }

    fun <T : Any, K : SettingsKey> get(easySetting: EasySetting<T, K>, key: K): T? {
        return easySettingRepository.get(easySetting, key)
    }

    fun <T : Any, K : SettingsKey> get(easySetting: EasySetting<T, K>, key: K, defaultValue: T): T {
        return easySettingRepository.get(easySetting, key) ?: defaultValue
    }

    fun <K : SettingsKey> decrement(easySetting: EasySetting<Long, K>, key: K): Long {
        return easySettingRepository.decrement(easySetting, key)
    }

    fun <K : SettingsKey> increment(easySetting: EasySetting<Long, K>, key: K): Long {
        return easySettingRepository.increment(easySetting, key)
    }
}
