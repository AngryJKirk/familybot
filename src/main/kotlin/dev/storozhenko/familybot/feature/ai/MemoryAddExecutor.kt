package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.ChatGPTMemory
import org.springframework.stereotype.Component

@Component
class MemoryAddExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig
) : Executor {


    override suspend fun execute(context: ExecutorContext) {
        val text = context.message.text
        if (text.isNullOrBlank()) {
            context.send("Пришли нормально")
            return
        }
        if (text.length > 500) {
            context.send("Говорили же меньше 500 символов. Ты прислал ${text.length}")
            return
        }

        val currentMemory = easyKeyValueService.get(ChatGPTMemory, context.chatKey, "")
        easyKeyValueService.put(ChatGPTMemory, context.chatKey, "$currentMemory\n$text")
        context.send("Готово")
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        val replyToMessage = context.update.message?.replyToMessage
        return replyToMessage != null && replyToMessage.from.userName == botConfig.botName
                && replyToMessage.text == "Напиши что добавить в ответ на это сообщение, 500 символов максимум"
    }

    override fun priority(context: ExecutorContext) = Priority.MEDIUM
}