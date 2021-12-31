package space.yaroslav.familybot

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
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.BotConfigInjector
import space.yaroslav.familybot.telegram.BotStarter
import space.yaroslav.familybot.telegram.FamilyBot

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
        val yandexKey = botConfigInjector.yandexKey?.takeIf(String::isNotBlank)
        if (yandexKey == null) {
            logger.warn("Yandex API key is not found, language API won't work")
        }
        val paymentToken = botConfigInjector.paymentToken?.takeIf(String::isNotBlank)
        if (paymentToken == null) {
            logger.warn("Payment token is not found, payment API won't work")
        }
        return BotConfig(
            notEmptyCheck(botConfigInjector.botToken, "botToken"),
            notEmptyCheck(botConfigInjector.botName, "botName"),
            notEmptyCheck(botConfigInjector.developer, "developer"),
            notEmptyCheck(botConfigInjector.developerId, "developerId"),
            botNameAliases,
            yandexKey,
            paymentToken,
            env.activeProfiles.contains(BotStarter.TESTING_PROFILE_NAME)
        )
    }

    private fun notEmptyCheck(value: String, valueName: String): String {
        if (value.isBlank()) {
            throw FamilyBot.InternalException("Value of '$valueName' must be not empty")
        }
        return value
    }
}

@Suppress("unused")
inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

fun main() {
    val app = SpringApplication(FamilyBotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run()
}
