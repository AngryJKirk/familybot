package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.BotOwnerPidorSkip
import org.springframework.stereotype.Component

@Component
class SkipBotOwnerSettingProcessor(private val easyKeyValueService: EasyKeyValueService) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.isFromDeveloper && context.update.getMessageTokens()[1] == "иммун"
    }

    override suspend fun process(context: ExecutorContext) {
        val currentSetting = easyKeyValueService.get(BotOwnerPidorSkip, context.chatKey) ?: false
        val newSetting = currentSetting.not()
        easyKeyValueService.put(BotOwnerPidorSkip, context.chatKey, newSetting)
        context.sender.send(context, "${currentSetting.toEmoji()} => ${newSetting.toEmoji()}")
    }
}
