package dev.storozhenko.familybot.feature.pidor.services

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class PidorAutoSelectScheduler(
    private val pidorAutoSelectService: PidorAutoSelectService,
    private val telegramClient: TelegramClient,
) {
    val log = KotlinLogging.logger {  }

    @Scheduled(cron = "0 0 10 * * *")
    fun start() {
        runCatching {
            pidorAutoSelectService.autoSelect(telegramClient)
        }.onFailure {
            log.error(it) { "Error while auto selecting pidor" }
        }
    }
}
