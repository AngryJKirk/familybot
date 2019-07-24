package space.yaroslav.familybot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.logging.BotLogger
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.logging.Level

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BotConfig::class)
class FamilybotApplication {

    @Bean
    fun botConfig(): DefaultBotOptions {
        return DefaultBotOptions()
    }

    @Bean
    fun telegramBot(bot: FamilyBot): TelegramBotsApi {
        val telegramBotsApi = TelegramBotsApi()
        telegramBotsApi.registerBot(bot)
        return telegramBotsApi
    }
}

fun main() {
    BotLogger.setLevel(Level.ALL)
    ApiContextInitializer.init()
    val app = SpringApplication(FamilybotApplication::class.java)
    app.isWebEnvironment = false
    app.run()
}
