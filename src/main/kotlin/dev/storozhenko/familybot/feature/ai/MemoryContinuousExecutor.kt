package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.ChatGPTMemory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage

@Component
class MemoryContinuousExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    botConfig: BotConfig
) : ContinuousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return setOf("Какое действие с ИИ памятью вы хотите выполнить? Эта память будет использована чтобы у бота был контекст при общении.")
    }

    override fun command() = Command.MEMORY

    override suspend fun execute(context: ExecutorContext) {
        context.client.execute(AnswerCallbackQuery(context.update.callbackQuery.id))
        val data = context.update.callbackQuery.data
        when (data) {
            "add" -> add(context)
            "show" -> show(context)
            "clear" -> clear(context)
        }
        context.client.execute(DeleteMessage(context.chat.idString, context.message.messageId))
    }

    private suspend fun show(context: ExecutorContext) {
        val memory = easyKeyValueService.get(ChatGPTMemory, context.chatKey)
        if (memory != null) {
            context.send("Текущая память: $memory")
        } else {
            context.send("Память отсутствует.")
        }
    }

    private suspend fun add(context: ExecutorContext) {
        context.send("Напиши что добавить в ответ на это сообщение, 500 символов максимум")
    }

    private suspend fun clear(context: ExecutorContext) {
        if (context.isFromAdmin()) {
            val memory = easyKeyValueService.getAndRemove(ChatGPTMemory, context.chatKey)
            if (memory != null) {
                context.send("Память удалена. Вот на память бэкап: $memory")
            } else {
                context.send("Нечего чистить, памяти нет.")
            }
        } else {
            context.send("Чистить память можно только админам")
        }
    }
}