package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.isFromAdmin
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.executors.command.settings.processors.SettingProcessor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AdvancedSettingsExecutor(
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
    private val processors: List<SettingProcessor>
) : CommandExecutor(botConfig) {
    override fun command() = Command.ADVANCED_SETTINGS

    private val log = getLogger()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val messageTokens = update.getMessageTokens()
        if (messageTokens.size == 1) {
            return {
                it.send(
                    update,
                    context.get(Phrase.ADVANCED_SETTINGS),
                    enableHtml = true
                )
            }
        }
        return {
            if (!it.isFromAdmin(update, botConfig)) {
                sendErrorMessage(
                    update, context.get(Phrase.ADVANCED_SETTINGS_ADMIN_ONLY)
                ).invoke(it)
            } else {
                runCatching {

                    val processor = processors
                        .find { processor -> processor.canProcess(update) }
                    return@runCatching processor
                        ?.process(update)
                        ?: sendErrorMessage(update)
                }.getOrElse { throwable ->
                    log.error("Advanced settings failed", throwable)
                    sendErrorMessage(update)
                }.invoke(it)
            }
        }
    }

    private fun sendErrorMessage(
        update: Update,
        message: String = dictionary.get(Phrase.ADVANCED_SETTINGS_ERROR, update)
    ): suspend (AbsSender) -> Unit {
        return {
            it.send(
                update,
                message
            )
        }
    }
}
