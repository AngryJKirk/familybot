package dev.storozhenko.familybot.feature.pidor.services

import dev.storozhenko.familybot.core.telegram.FamilyBot
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PidorAutoSelectScheduler(
    private val pidorAutoSelectService: PidorAutoSelectService,
    private val familyBot: FamilyBot,
) {
    val log = KotlinLogging.logger {  }

    @Scheduled(cron = "0 0 10 * * *")
    fun start() {
        runCatching {
            pidorAutoSelectService.autoSelect(familyBot)
        }.onFailure {
            log.error(it) { "Error while auto selecting pidor" }
        }
    }
}
