package space.yaroslav.familybot.services.settings

import kotlin.reflect.KClass

interface EasySetting<T : Any> {

    fun getName(): String = this::class.java.simpleName
    fun getType(): KClass<T>
}
