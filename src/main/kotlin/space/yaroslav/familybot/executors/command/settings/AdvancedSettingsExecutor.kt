package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.isFromAdmin
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
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
        val splitMessage = update.message.text.split(" ")
        if (splitMessage.size == 1) {
            return {
                it.send(
                    update,
                    dictionary.get(Phrase.ADVANCED_SETTINGS, update),
                    enableHtml = true
                )
            }
        }
        return {
            if (!it.isFromAdmin(update, botConfig)) {
                sendErrorMessage(
                    update,
                    "Ты кого наебать хочешь? Ты ведь не админ даже, а так, ПУСТЫШКА, пародия на личность, позови старшего, если хочешь что-то изменить в этой жизни. Ведь большего ты и не достоин, кроме как всегда полагаться на кого-то, кто тебе поможет. Задумайся, ведь так было всегда, ты всегда был слаб и звал на помощь сильного, вот и сейчас, беги, зови свою МАМОЧКУ или ПАПОЧКУ, чтобы тебе подтерли задницу. Я буду говорить только с настоящими лидерами."
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
