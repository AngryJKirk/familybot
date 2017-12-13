package space.yaroslav.familybot

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(BotConfig::class)
class SpringConfiguration