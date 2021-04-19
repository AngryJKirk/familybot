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
        val context = dictionary.createContext(update)
        val value = update.getMessageTokens()[2]
        val amountOfDensity = value.toLongOrNull() ?: return {
            it.send(
                update,
                context.get(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NOT_NUMBER)
                    .replace("#value", value)
            )
        }

        if (amountOfDensity < 0) {
            return {
                it.send(update, context.get(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NEGATIVE))
            }
        }

        easySettingsService.put(TalkingDensity, update.toChat().key(), amountOfDensity)
        return {
            it.send(update, context.get(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
