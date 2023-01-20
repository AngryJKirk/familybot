package dev.storozhenko.familybot.executors.command.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.BotOwnerPidorSkip
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class SkipBotOwnerSettingProcessor(private val easyKeyValueService: EasyKeyValueService) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.isFromDeveloper && context.update.getMessageTokens()[1] == "иммун"
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val currentSetting = easyKeyValueService.get(BotOwnerPidorSkip, context.chatKey) ?: false
        val newSetting = currentSetting.not()
        easyKeyValueService.put(BotOwnerPidorSkip, context.chatKey, newSetting)
        return {
            it.send(context, "${currentSetting.toEmoji()} => ${newSetting.toEmoji()}")
        }
    }
}