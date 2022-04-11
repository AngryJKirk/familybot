package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.pidor.PidorAutoSelectService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ManualPidorSelectExecutor(
    private val pidorAutoSelectService: PidorAutoSelectService,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "pidor_manual"

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {

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