package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.isFromAdmin
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.executors.command.settings.processors.SettingProcessor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AdvancedSettingsExecutor(
    private val botConfig: BotConfig,
    private val processors: List<SettingProcessor>
) : CommandExecutor() {
    override fun command() = Command.ADVANCED_SETTINGS

    private val log = getLogger()

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        
        val messageTokens = context.update.getMessageTokens()
        if (messageTokens.size == 1) {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.ADVANCED_SETTINGS),
                    enableHtml = true
                )
            }
        }
        return {
            if (!it.isFromAdmin(context)) {
                sendErrorMessage(
                    context, context.phrase(Phrase.ADVANCED_SETTINGS_ADMIN_ONLY)
                ).invoke(it)
            } else {
                runCatching {

                    val processor = processors
                        .find { processor -> processor.canProcess(context) }
                    return@runCatching processor
                        ?.process(context)
                        ?: sendErrorMessage(context)
                }.getOrElse { throwable ->
                    log.error("Advanced settings failed", throwable)
                    sendErrorMessage(context)
                }.invoke(it)
            }
        }
    }

    private fun sendErrorMessage(
        context: ExecutorContext,
        message: String = context.phrase(Phrase.ADVANCED_SETTINGS_ERROR)
    ): suspend (AbsSender) -> Unit {
        return {
            it.send(
                context,
                message
            )
        }
    }
}
