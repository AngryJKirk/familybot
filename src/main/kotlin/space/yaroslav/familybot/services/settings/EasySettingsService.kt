package space.yaroslav.familybot.services.settings

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EasySettingsService(private val easySettingRepository: EasySettingsRedisRepository) {

    fun <T : Any> put(easySetting: EasySetting<T>, key: SettingsKey, value: T, duration: Duration? = null) {
        easySettingRepository.put(easySetting, key, value, duration)
    }

    fun <T : Any> get(easySetting: EasySetting<T>, key: SettingsKey): T? {
        return easySettingRepository.get(easySetting, key)
    }

    fun <T : Any> get(easySetting: EasySetting<T>, key: SettingsKey, defaultValue: T): T {
        return easySettingRepository.get(easySetting, key) ?: defaultValue
    }

    fun decrement(easySetting: EasySetting<Long>, key: SettingsKey): Long {
        return easySettingRepository.decrement(easySetting, key)
    }

    fun increment(easySetting: EasySetting<Long>, key: SettingsKey): Long {
        return easySettingRepository.increment(easySetting, key)
    }
}
