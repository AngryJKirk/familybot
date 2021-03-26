package space.yaroslav.familybot.services.settings

import java.time.Duration

interface EasySettingsRepository {
    fun <T : Any> put(
        easySetting: EasySetting<T>,
        key: SettingsKey,
        value: T,
        duration: Duration? = null
    )

    fun <T : Any> get(easySetting: EasySetting<T>, key: SettingsKey): T?
    fun decrement(easySetting: EasySetting<Long>, key: SettingsKey): Long
    fun increment(easySetting: EasySetting<Long>, key: SettingsKey): Long
}
