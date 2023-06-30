package dev.storozhenko.familybot.core.keyvalue.models

import java.time.Instant

interface EasyKeyType<INPUT : Any, KEY : EasyKey> {
    fun getName(): String = this::class.java.simpleName
    fun mapToString(value: INPUT): String
    fun mapFromString(value: String): INPUT
}

interface BooleanKeyType<KEY : EasyKey> : EasyKeyType<Boolean, KEY> {
    override fun mapToString(value: Boolean) = value.toString()
    override fun mapFromString(value: String) = value.toBoolean()
}

interface LongKeyType<KEY : EasyKey> : EasyKeyType<Long, KEY> {
    override fun mapToString(value: Long) = value.toString()
    override fun mapFromString(value: String) = value.toLong()
}

interface StringKeyType<KEY : EasyKey> : EasyKeyType<String, KEY> {
    override fun mapToString(value: String) = value
    override fun mapFromString(value: String) = value
}

interface InstantKeyType<KEY : EasyKey> : EasyKeyType<Instant, KEY> {
    override fun mapToString(value: Instant) = value.epochSecond.toString()
    override fun mapFromString(value: String): Instant = Instant.ofEpochSecond(value.toLong())
}
