package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.processors.SettingProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class AdvancedSettingsExecutor(
    private val processors: List<SettingProcessor>,
) : CommandExecutor() {
    override fun command() = Command.ADVANCED_SETTINGS

    private val log = KotlinLogging.logger {  }

    override suspend fun execute(context: ExecutorContext) {
        val messageTokens = context.update.getMessageTokens()
        if (messageTokens.size == 1) {
            context.sender.send(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS),
                enableHtml = true,
            )
            return
        }
        if (!context.sender.isFromAdmin(context)) {
            sendErrorMessage(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS_ADMIN_ONLY),
            )
        } else {
            runCatching {
                val processor = processors
                    .find { processor -> processor.canProcess(context) }
                return@runCatching processor
                    ?.process(context)
                    ?: sendErrorMessage(context)
            }.getOrElse { throwable ->
                log.error(throwable) { "Advanced settings failed" }
                sendErrorMessage(context)
            }
        }
    }

    private suspend fun sendErrorMessage(
        context: ExecutorContext,
        message: String = context.phrase(Phrase.ADVANCED_SETTINGS_ERROR),
    ) {
        context.sender.send(context, message)
    }
}
