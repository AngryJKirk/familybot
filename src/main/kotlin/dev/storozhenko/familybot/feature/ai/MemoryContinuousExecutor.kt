package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.common.extensions.send
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
        return setOf("Какое действие с ИИ памятью вы хотите выполнить?")
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
            context.client.send(context, "Текущая память: $memory")
        } else {
            context.client.send(context, "Память отсутствует.")
        }
    }

    private suspend fun add(context: ExecutorContext) {
        context.client.send(context, "Напиши что добавить в ответ на это сообщение, 500 символов максимум")
    }

    private suspend fun clear(context: ExecutorContext) {
        if (context.client.isFromAdmin(context)) {
            val memory = easyKeyValueService.getAndRemove(ChatGPTMemory, context.chatKey)
            if (memory != null) {
                context.client.send(context, "Память удалена. Вот на память бэкап: $memory")
            } else {
                context.client.send(context, "Нечего чистить, памяти нет.")
            }
        } else {
            context.client.send(context, "Чистить память можно только админам")
        }
    }
}