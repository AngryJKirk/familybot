package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.isFromAdmin
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.TalkingDencity
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AdvancedSettings(
    private val easySettingsService: EasySettingsService,
    private val dictionary: Dictionary,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.ADVANCED_SETTINGS

    private val log = getLogger()

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val splitMessage = update.message.text.split(" ")
        if (splitMessage.size == 1) {
            return {
                it.send(
                    update,
                    dictionary.get(Phrase.ADVANCED_SETTINGS),
                    enableHtml = true
                )
            }
        }
        return {
            if (!it.isFromAdmin(update)) {
                sendErrorMessage(
                    update,
                    "Ты кого наебать хочешь? Ты ведь не админ даже, а так, ПУСТЫШКА, пародия на личность, позови старшего, если хочешь что-то изменить в этой жизни. Ведь большего ты и не достоин, кроме как всегда полагаться на кого-то, кто тебе поможет. Задумайся, ведь так было всегда, ты всегда был слаб и звал на помощь сильного, вот и сейчас, беги, зови свою МАМОЧКУ или ПАПОЧКУ, чтобы тебе подтерли задницу. Я буду говорить только с настоящими лидерами."
                )
            } else {
                runCatching {
                    when (splitMessage[1]) {
                        "разговорчики" -> setTalkingDensity(update, splitMessage[2])
                        else -> sendErrorMessage(update)
                    }
                }.getOrElse { throwable ->
                    log.error("Advanced settings failed", throwable)
                    sendErrorMessage(update)
                }.invoke(it)
            }

        }
    }

    private fun setTalkingDensity(update: Update, value: String): suspend (AbsSender) -> Unit {
        val amountOfDensity = value.toLongOrNull() ?: return sendErrorMessage(
            update,
            "Я твоей матери на спине написал $value когда ебал ее, научись блять читать как пользоваться командой"
        )

        if (amountOfDensity < 0) {
            return sendErrorMessage(
                update,
                "Ровно столько раз я колол твою мамашу, только со знаком плюс."
            )
        }

        easySettingsService.put(TalkingDencity, update.toChat().key(), amountOfDensity)
        return getOkMessage(update)
    }

    private fun sendErrorMessage(
        update: Update,
        message: String = dictionary.get(Phrase.ADVANCED_SETTINGS_ERROR)
    ): suspend (AbsSender) -> Unit {
        return {
            it.send(
                update,
                message,
                shouldTypeBeforeSend = true
            )
        }
    }

    private fun getOkMessage(update: Update): suspend (AbsSender) -> Unit {
        return {
            it.send(
                update,
                dictionary.get(Phrase.ADVANCED_SETTINGS_OK)
            )
        }
    }
}