package dev.storozhenko.familybot.services.settings

import kotlin.reflect.KClass

interface EasyKeyType<T : Any, K : EasyKey> {

    fun getName(): String = this::class.java.simpleName
    fun getType(): KClass<T>
}
