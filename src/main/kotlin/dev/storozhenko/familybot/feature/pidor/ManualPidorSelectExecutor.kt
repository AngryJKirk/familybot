package dev.storozhenko.familybot.feature.pidor

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.services.PidorAutoSelectService
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
