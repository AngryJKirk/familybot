package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.TalkingDensity
import org.springframework.stereotype.Component

@Component
class TalkingDensitySettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    private val commands = setOf("разговорчики", "балачки")
    override fun canProcess(context: ExecutorContext): Boolean {
        val command = context.update.getMessageTokens()[1]
        return commands.contains(command)
    }

    override suspend fun process(context: ExecutorContext) {
        val value = context.update.getMessageTokens()[2]
        val amountOfDensity = value.toLongOrNull()
        if (amountOfDensity == null) {
            context.sender.send(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NOT_NUMBER)
                    .replace("#value", value)
            )
            return
        }

        if (amountOfDensity < 0) {
            context.sender.send(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NEGATIVE)
            )
        }

        easyKeyValueService.put(TalkingDensity, context.chatKey, amountOfDensity)
        context.sender.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
    }
}
