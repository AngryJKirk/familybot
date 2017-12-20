package space.yaroslav.familybot.repos.ifaces

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Config
import space.yaroslav.familybot.common.ConfigType
import space.yaroslav.familybot.common.KeywordConfig
import java.time.Instant

@Component
class InCodeConfigRepository : ConfigRepository {

    private var config: Config = KeywordConfig()

    override fun set(config: Config) {
        this.config = config
    }

    override fun get(type: ConfigType): Config {
        val keywordConfig = config as KeywordConfig
        if(keywordConfig.ttl.isBefore(Instant.now())){
            this.config = KeywordConfig()
        }
        return config
    }
}