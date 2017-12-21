package space.yaroslav.familybot.repos

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.Config
import space.yaroslav.familybot.common.ConfigType
import space.yaroslav.familybot.common.KeywordConfig
import space.yaroslav.familybot.repos.ifaces.ConfigRepository
import java.time.Instant

@Component
class InCodeConfigRepository : ConfigRepository {

    private val configs: MutableMap<Chat, Config> = HashMap()

    init {

    }

    override fun set(config: Config) {
        configs.plus(config.chat to config)
    }

    override fun get(type: ConfigType, chat: Chat): Config {
        val keywordConfig = configs.getOrPut(chat, { KeywordConfig(chat = chat) }) as KeywordConfig
        if (keywordConfig.ttl.isBefore(Instant.now())) {
            this.configs.plus(chat to KeywordConfig(chat = chat))
        }
        return configs[chat]!!
    }
}