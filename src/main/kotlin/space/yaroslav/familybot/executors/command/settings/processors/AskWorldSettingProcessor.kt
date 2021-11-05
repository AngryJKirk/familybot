package space.yaroslav.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.AskWorldDensity
import space.yaroslav.familybot.services.settings.EasyKeyValueService

@Component
class AskWorldSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
    private val functionsConfigureRepository: FunctionsConfigureRepository
) : SettingProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        return context.update.getMessageTokens()[1] == "вопросики"
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {

        val arg = context.update.getMessageTokens()[2]
        val density = AskWorldDensityValue.values().find { mode -> mode.text == arg }
            ?: return { sender ->
                sender.send(
                    context,
                    context.phrase(Phrase.ADVANCED_SETTINGS_ASK_WORLD_BAD_USAGE)
                )
            }
        return { sender ->
            functionsConfigureRepository.setStatus(
                FunctionId.ASK_WORLD,
                context,
                isEnabled = density != AskWorldDensityValue.NONE
            )
            easyKeyValueService.put(AskWorldDensity, context.chatKey, density.text)
            sender.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}

enum class AskWorldDensityValue(val text: String) {
    ALL("все"),
    LESS("поменьше"),
    NONE("отключить")
}
