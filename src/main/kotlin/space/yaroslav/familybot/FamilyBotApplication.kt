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

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BotConfigInjector::class)
class FamilyBotApplication(
    private val env: ConfigurableEnvironment
) {

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }

    @Bean
    fun injectBotConfig(botConfigInjector: BotConfigInjector): BotConfig {
        val botNameAliases = if (botConfigInjector.botNameAliases.isNullOrEmpty()) {
            listOf(botConfigInjector.botName)
        } else {
            botConfigInjector.botNameAliases.split(",")
        }
        return BotConfig(
            botConfigInjector.botToken,
            botConfigInjector.botName,
            botConfigInjector.developer,
            botConfigInjector.developerId,
            botNameAliases,
            botConfigInjector.yandexKey?.takeIf(String::isNotBlank),
            botConfigInjector.paymentToken?.takeIf(String::isNotBlank),
            env.activeProfiles.contains(BotStarter.TESTING_PROFILE_NAME)
        )
    }
}

@Suppress("unused")
inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

fun main() {
    val app = SpringApplication(FamilyBotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run()
}
