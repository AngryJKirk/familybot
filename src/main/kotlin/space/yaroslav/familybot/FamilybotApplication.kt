package space.yaroslav.familybot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.logging.BotLogger
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.logging.Level

@SpringBootApplication
@EnableAutoConfiguration
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

fun main(args: Array<String>) {
    BotLogger.setLevel(Level.ALL)
    ApiContextInitializer.init()
    val app = SpringApplication(FamilybotApplication::class.java)
    app.isWebEnvironment = true
    app.run()
}