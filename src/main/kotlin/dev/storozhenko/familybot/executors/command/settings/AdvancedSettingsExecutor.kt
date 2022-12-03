package dev.storozhenko.familybot.executors.command.settings

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.executors.command.settings.processors.SettingProcessor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Command
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class AdvancedSettingsExecutor(
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
                    context,
                    context.phrase(Phrase.ADVANCED_SETTINGS_ADMIN_ONLY)
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
