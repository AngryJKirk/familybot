package dev.storozhenko.familybot.executors.pm

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.pidor.PidorAutoSelectService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class ManualPidorSelectExecutor(
    private val pidorAutoSelectService: PidorAutoSelectService
) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "pidor_manual"

    override fun executeInternal(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val response = runCatching {
                pidorAutoSelectService.autoSelect(it)
                "it's done"
            }
                .onFailure { exception -> exception.message }
            it.send(context, response.getOrDefault("error"))
        }
    }
}
