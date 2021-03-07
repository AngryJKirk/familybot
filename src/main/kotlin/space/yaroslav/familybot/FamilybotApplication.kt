package space.yaroslav.familybot

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot

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
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(bot)
        return telegramBotsApi
    }
}

fun main() {
    val app = SpringApplication(FamilybotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run()
}
