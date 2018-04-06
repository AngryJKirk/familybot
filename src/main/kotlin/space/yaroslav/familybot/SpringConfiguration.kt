package space.yaroslav.familybot

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import space.yaroslav.familybot.telegram.BotConfig

@Configuration
@EnableScheduling
@EnableConfigurationProperties(BotConfig::class)
class SpringConfiguration