package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getMessageTokens
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.AskWorldDensity
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class AskWorldSettingProcessor(
    private val dictionary: Dictionary,
    private val easySettingsService: EasySettingsService,
    private val functionsConfigureRepository: FunctionsConfigureRepository
) : SettingProcessor {

    override fun canProcess(update: Update): Boolean {
        return update.getMessageTokens()[1] == "вопросики"
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val arg = update.getMessageTokens()[2]
        val density = AskWorldDensityValue.values().find { mode -> mode.text == arg }
            ?: return { sender ->
                sender.send(
                    update,
                    context.get(Phrase.ADVANCED_SETTINGS_ASK_WORLD_BAD_USAGE)
                )
            }
        return { sender ->
            val chat = update.toChat()
            functionsConfigureRepository.setStatus(
                FunctionId.ASK_WORLD,
                chat,
                isEnabled = density != AskWorldDensityValue.NONE
            )
            easySettingsService.put(AskWorldDensity, chat.key(), density.text)
            sender.send(update, context.get(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}

enum class AskWorldDensityValue(val text: String) {
    ALL("все"),
    LESS("поменьше"),
    NONE("отключить")
}