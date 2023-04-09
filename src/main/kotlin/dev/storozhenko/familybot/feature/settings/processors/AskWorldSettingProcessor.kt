package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.feature.settings.models.AskWorldDensity
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

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
                context.chat,
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
