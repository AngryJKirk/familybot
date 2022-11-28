package dev.storozhenko.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.TalkingDensity

@Component
class TalkingDensitySettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    private val commands = setOf("разговорчики", "балачки")
    override fun canProcess(context: ExecutorContext): Boolean {
        val command = context.update.getMessageTokens()[1]
        return commands.contains(command)
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {

        val value = context.update.getMessageTokens()[2]
        val amountOfDensity = value.toLongOrNull() ?: return {
            it.send(
                context,
                context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NOT_NUMBER)
                    .replace("#value", value)
            )
        }

        if (amountOfDensity < 0) {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NEGATIVE)
                )
            }
        }

        easyKeyValueService.put(TalkingDensity, context.chatKey, amountOfDensity)
        return {
            it.send(context, context.phrase(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
