package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Config
import space.yaroslav.familybot.common.ConfigType


interface ConfigRepository {

    fun set(config: Config)

    fun get(type: ConfigType, chat: Chat): Config

}