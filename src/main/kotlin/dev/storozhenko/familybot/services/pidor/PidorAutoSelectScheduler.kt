package dev.storozhenko.familybot.services.pidor

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.telegram.FamilyBot

@Component
class PidorAutoSelectScheduler(
    private val pidorAutoSelectService: PidorAutoSelectService,
    private val familyBot: FamilyBot
) {
    val log = getLogger()

    @Scheduled(cron = "0 0 10 * * *")
    fun start() {
        runCatching {
            pidorAutoSelectService.autoSelect(familyBot)
        }.onFailure {
            log.error("Error while auto selecting pidor", it)
        }
    }
}
