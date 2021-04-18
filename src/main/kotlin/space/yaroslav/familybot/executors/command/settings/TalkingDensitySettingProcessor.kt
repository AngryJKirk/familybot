package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getMessageTokens
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.TalkingDensity
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class TalkingDensitySettingProcessor(
    private val easySettingsService: EasySettingsService,
    private val dictionary: Dictionary
) : SettingProcessor {

    private val commands = setOf("разговорчики", "балачки")
    override fun canProcess(update: Update): Boolean {
        val command = update.getMessageTokens()[1]
        return commands.contains(command)
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        val value = update.getMessageTokens()[2]
        val amountOfDensity = value.toLongOrNull() ?: return {
            it.send(
                update,
                "Я твоей матери на спине написал $value когда ебал ее, научись блять читать как пользоваться командой"
            )
        }

        if (amountOfDensity < 0) {
            return {
                it.send(update, "Ровно столько раз я колол твою мамашу, только со знаком плюс.")
            }
        }

        easySettingsService.put(TalkingDensity, update.toChat().key(), amountOfDensity)
        return {
            it.send(update, dictionary.get(Phrase.ADVANCED_SETTINGS_OK, update))
        }
    }
}