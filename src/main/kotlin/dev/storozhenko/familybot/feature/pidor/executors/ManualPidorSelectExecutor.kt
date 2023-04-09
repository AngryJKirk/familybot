package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.services.PidorAutoSelectService
import org.springframework.stereotype.Component

@Component
class ManualPidorSelectExecutor(
    private val pidorAutoSelectService: PidorAutoSelectService
) : OnlyBotOwnerExecutor() {
    override fun getMessagePrefix() = "pidor_manual"

    override suspend fun executeInternal(context: ExecutorContext) {
        val response = runCatching {
            pidorAutoSelectService.autoSelect(context.sender)
            "it's done"
        }
            .onFailure { exception -> exception.message }
        context.sender.send(context, response.getOrDefault("error"))
    }
}
