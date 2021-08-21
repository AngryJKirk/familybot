package space.yaroslav.familybot.telegram

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
@Profile(BotStarter.NOT_TESTING_PROFILE_NAME)
class BotStarter {

    companion object Profile {
        const val TESTING_PROFILE_NAME = "testing"
        const val NOT_TESTING_PROFILE_NAME = "!$TESTING_PROFILE_NAME"
    }

    @EventListener(ApplicationReadyEvent::class)
    fun telegramBot(event: ApplicationReadyEvent) {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(event.applicationContext.getBean(FamilyBot::class.java))
    }
}
