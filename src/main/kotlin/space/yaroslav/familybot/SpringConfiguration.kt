package space.yaroslav.familybot

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import space.yaroslav.familybot.telegram.BotConfig


@Configuration
@EnableConfigurationProperties(BotConfig::class)
class SpringConfiguration