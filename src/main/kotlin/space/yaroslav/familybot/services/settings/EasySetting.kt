package space.yaroslav.familybot.services.settings

import kotlin.reflect.KClass

interface EasySetting<T : Any, K : SettingsKey> {

    fun getName(): String = this::class.java.simpleName
    fun getType(): KClass<T>
    fun keyType(): KClass<K>
}
