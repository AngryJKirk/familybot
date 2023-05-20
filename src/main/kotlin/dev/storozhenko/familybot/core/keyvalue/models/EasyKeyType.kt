package dev.storozhenko.familybot.core.keyvalue.models

import java.time.Instant

interface EasyKeyType<INPUT : Any, KEY : EasyKey> {
    fun getName(): String = this::class.java.simpleName
    fun getMapper(): EasyKeyTypeMapper<INPUT>
}

interface EasyKeyTypeMapper<INPUT> {
    fun mapToString(value: INPUT): String
    fun mapFromString(value: String): INPUT
}

class BooleanMapper : EasyKeyTypeMapper<Boolean> {
    override fun mapToString(value: Boolean) = value.toString()
    override fun mapFromString(value: String) = value.toBoolean()
}

class StringMapper : EasyKeyTypeMapper<String> {
    override fun mapToString(value: String) = value
    override fun mapFromString(value: String) = value
}

class LongMapper : EasyKeyTypeMapper<Long> {
    override fun mapToString(value: Long) = value.toString()
    override fun mapFromString(value: String) = value.toLong()
}

class InstantMapper : EasyKeyTypeMapper<Instant> {
    override fun mapToString(value: Instant) = value.epochSecond.toString()

    override fun mapFromString(value: String): Instant = Instant.ofEpochSecond(value.toLong())

}


interface BooleanKeyType<KEY : EasyKey> : EasyKeyType<Boolean, KEY> {
    companion object {
        val booleanMapper: BooleanMapper = BooleanMapper()
    }

    override fun getMapper() = booleanMapper
}

interface LongKeyType<KEY : EasyKey> : EasyKeyType<Long, KEY> {
    companion object {
        val mapper: LongMapper = LongMapper()
    }

    override fun getMapper() = mapper
}

interface StringKeyType<KEY : EasyKey> : EasyKeyType<String, KEY> {
    companion object {
        val mapper: StringMapper = StringMapper()
    }

    override fun getMapper() = mapper
}

interface InstantKeyType<KEY : EasyKey> : EasyKeyType<Instant, KEY> {
    companion object {
        val mapper: InstantMapper = InstantMapper()
    }

    override fun getMapper() = mapper
}
