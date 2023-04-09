package dev.storozhenko.familybot.core.keyvalue.models

import kotlin.reflect.KClass

interface EasyKeyType<T : Any, K : EasyKey> {

    fun getName(): String = this::class.java.simpleName
    fun getType(): KClass<T>
}

interface BooleanKeyType<K : EasyKey> : EasyKeyType<Boolean, K> {
    override fun getType() = Boolean::class
}

interface LongKeyType<K : EasyKey> : EasyKeyType<Long, K> {
    override fun getType() = Long::class
}

interface StringKeyType<K : EasyKey> : EasyKeyType<String, K> {
    override fun getType() = String::class
}