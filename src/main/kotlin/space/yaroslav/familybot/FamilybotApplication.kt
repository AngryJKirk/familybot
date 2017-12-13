package space.yaroslav.familybot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.bots.DefaultBotOptions


@SpringBootApplication
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
    ApiContextInitializer.init()
    val app = SpringApplication(FamilybotApplication::class.java)
    app.isWebEnvironment = false
    app.run()
}