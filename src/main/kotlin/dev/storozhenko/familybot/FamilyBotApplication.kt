package dev.storozhenko.familybot

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.scheduling.annotation.EnableScheduling
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.BotConfigInjector
import dev.storozhenko.familybot.telegram.BotStarter
import dev.storozhenko.familybot.telegram.FamilyBot

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BotConfigInjector::class)
class FamilyBotApplication(
    private val env: ConfigurableEnvironment
) {
    private val logger = getLogger()

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    @Bean
    fun injectBotConfig(botConfigInjector: BotConfigInjector): BotConfig {
        val botNameAliases = if (botConfigInjector.botNameAliases.isNullOrEmpty()) {
            logger.warn("No bot aliases provided, using botName")
            listOf(botConfigInjector.botName)
        } else {
            botConfigInjector.botNameAliases.split(",")
        }
        return BotConfig(
            notEmptyCheck(botConfigInjector.botToken, "botToken"),
            notEmptyCheck(botConfigInjector.botName, "botName"),
            notEmptyCheck(botConfigInjector.developer, "developer"),
            notEmptyCheck(botConfigInjector.developerId, "developerId"),
            botNameAliases,
            notEmptyCheckAllowOptional(
                botConfigInjector::yandexKey,
                "Yandex API key is not found, language API won't work"
            ),
            notEmptyCheckAllowOptional(
                botConfigInjector::paymentToken,
                "Payment token is not found, payment API won't work"
            ),
            env.activeProfiles.contains(BotStarter.TESTING_PROFILE_NAME),
            notEmptyCheckAllowOptional(
                botConfigInjector::ytdlLocation,
                "yt-dlp is missing, downloading function won't work"
            )
        )
    }

    private fun notEmptyCheck(value: String, valueName: String): String {
        if (value.isBlank()) {
            throw FamilyBot.InternalException("Value of '$valueName' must be not empty")
        }
        return value
    }

    private fun notEmptyCheckAllowOptional(value: () -> String?, log: String): String? {
        return value()?.takeIf(String::isNotBlank).also {
            if(it == null){
              logger.warn(log)
            }
        }
    }
}

@Suppress("unused")
inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

fun main() {
    val app = SpringApplication(FamilyBotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run()
}
