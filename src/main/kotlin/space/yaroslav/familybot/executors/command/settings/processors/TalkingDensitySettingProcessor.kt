package space.yaroslav.familybot.executors.command.settings.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TalkingDensity

@Component
class TalkingDensitySettingProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : SettingProcessor {

    private val commands = setOf("разговорчики", "балачки")
    override fun canProcess(executorContext: ExecutorContext): Boolean {
        val command = executorContext.update.getMessageTokens()[1]
        return commands.contains(command)
    }

    override fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        val value = executorContext.update.getMessageTokens()[2]
        val amountOfDensity = value.toLongOrNull() ?: return {
            it.send(
                executorContext,
                executorContext.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NOT_NUMBER)
                    .replace("#value", value)
            )
        }

        if (amountOfDensity < 0) {
            return {
                it.send(
                    executorContext,
                    executorContext.phrase(Phrase.ADVANCED_SETTINGS_FAILED_TALKING_DENSITY_NEGATIVE)
                )
            }
        }

        easyKeyValueService.put(TalkingDensity, executorContext.update.toChat().key(), amountOfDensity)
        return {
            it.send(executorContext, executorContext.phrase(Phrase.ADVANCED_SETTINGS_OK))
        }
    }
}
