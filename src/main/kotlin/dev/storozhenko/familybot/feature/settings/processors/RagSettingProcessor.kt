package dev.storozhenko.familybot.feature.settings.processors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.RagContext
import org.springframework.stereotype.Component

@Component
class RagSettingProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : SettingProcessor {
    override fun canProcess(context: ExecutorContext): Boolean {
        return context.isFromDeveloper && context.update.getMessageTokens()[1] == "rag"
    }

    override suspend fun process(context: ExecutorContext) {
        if (context.update.getMessageTokens().size < 3) {
            context.send("Not ok")
            return
        }
        val state = context.update.getMessageTokens()[2]
        if (state == "вкл") {
            easyKeyValueService.put(RagContext, context.chatKey, true)
            context.send("Ok")
            return
        }

        if (state == "выкл") {
            easyKeyValueService.put(RagContext, context.chatKey, false)
            context.send("Ok")
            return
        }

        context.send("Not ok")


    }
}