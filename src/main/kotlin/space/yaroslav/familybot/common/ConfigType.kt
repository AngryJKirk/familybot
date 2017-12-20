package space.yaroslav.familybot.common

import kotlin.reflect.KClass


enum class ConfigType(val clazz: KClass<out Config>) {

    KEYWORD(KeywordConfig::class)

}