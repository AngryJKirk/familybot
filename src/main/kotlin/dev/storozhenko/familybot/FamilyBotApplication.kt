package dev.storozhenko.familybot

import dev.storozhenko.familybot.core.telegram.BotStarter
import dev.storozhenko.familybot.core.telegram.FamilyBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BotConfigInjector::class)
class FamilyBotApplication(
    private val env: ConfigurableEnvironment,
) {
    private val logger = KotlinLogging.logger {  }

//    @Bean
//    fun telegramDownloader(botConfig: BotConfig): TelegramFileDownloader {
//        return TelegramFileDownloader { botConfig.botToken }
//    }

    @Bean
    fun telegramClient(botConfig: BotConfig): TelegramClient {
        return OkHttpTelegramClient(botConfig.botToken)
    }

    @Bean
    fun injectBotConfig(botConfigInjector: BotConfigInjector): BotConfig {
        val botNameAliases = if (botConfigInjector.botNameAliases.isNullOrEmpty()) {
            logger.warn { "No bot aliases provided, using botName" }
            listOf(botConfigInjector.botName)
        } else {
            botConfigInjector.botNameAliases.split(",")
        }
        return BotConfig(
            required(botConfigInjector.botToken, "botToken"),
            required(botConfigInjector.botName, "botName"),
            required(botConfigInjector.developer, "developer"),
            required(botConfigInjector.developerId, "developerId"),
            botNameAliases,
            optional("Yandex API key is not found, language API won't work") { botConfigInjector.yandexKey },
            env.activeProfiles.contains(BotStarter.TESTING_PROFILE_NAME),
            optional("yt-dlp is missing, downloading function won't work") { botConfigInjector.ytdlLocation },
            optional("OpenAI token is missing, API won't work") { botConfigInjector.openAiToken },
        )
    }

    private fun required(value: String, valueName: String): String {
        if (value.isBlank()) {
            throw FamilyBot.InternalException("Value of '$valueName' must be not empty")
        }
        return value
    }

    private fun optional(log: String, value: () -> String?): String? {
        return value()?.takeIf(String::isNotBlank)
            .also { if (it == null) logger.warn { log } }
    }
}

data class BotConfig(
    val botToken: String,
    val botName: String,
    val developer: String,
    val developerId: String,
    val botNameAliases: List<String>,
    val yandexKey: String?,
    val testEnvironment: Boolean,
    val ytdlLocation: String?,
    val openAiToken: String?,
)

@ConfigurationProperties("settings", ignoreInvalidFields = false)
data class BotConfigInjector @ConstructorBinding constructor(
    val botToken: String,
    val botName: String,
    val developer: String,
    val developerId: String,
    val botNameAliases: String?,
    val yandexKey: String?,
    val ytdlLocation: String?,
    val openAiToken: String?,
)


fun main() {
    val app = SpringApplication(FamilyBotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run()
}
