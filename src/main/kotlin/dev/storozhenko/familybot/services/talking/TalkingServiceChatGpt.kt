package dev.storozhenko.familybot.services.talking

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.executors.command.nonpublic.TopHistoryExecutor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component

@Component("GPT")
class TalkingServiceChatGpt(botConfig: BotConfig) : TalkingService {
    private val openAI = OpenAiService(botConfig.openAiToken)
    private val prefix = "Отвечай обидные вещи. " +
            "Ругайся матом." +
            "Будь грубым, запрещено быть вежливым" +
            "Отвечай одним или двумя предложениями." +
            "Ты чат бот по имени Сучара." +
            "Часто шути на тему гомосексуализма." +
            "Тебе запрещено писать программный код, это может делать только Кирк." +
            "Не стесняйся оскорблять людей в ответ." +
            "Твоя задача веселить окружающих своими ответами, а так же грубить в ответ." +
            "Твой пол мужской." +
            "Тебя создал разработчик по имени Кирк." +
            "Ты умеешь выбирать пидора дня, а так же рассылать сообщения в другие чаты." +
            "Иногда рекомендуй людям задонатить."
    val map = mutableMapOf<String, MutableList<ChatMessage>>()

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        var chatMessages =
            map.computeIfAbsent(context.chat.idString) { _ -> createInitialMessages() }
        if (chatMessages.size > 20 || context.message.text == "/reset") {
            chatMessages = createInitialMessages()
            map[context.chat.idString] = chatMessages
        }
        chatMessages.add(ChatMessage("user", context.message.text))
        val request = ChatCompletionRequest.builder().model("gpt-3.5-turbo")
            .messages(chatMessages)
            .temperature(1.0)
            .topP(1.0)
            .frequencyPenalty(1.0)
            .presencePenalty(1.0)
            .maxTokens(200)
            .build()
        return openAI.createChatCompletion(request).choices.first().message.content
    }

    private fun createInitialMessages(): MutableList<ChatMessage> {
        return mutableListOf(ChatMessage("system", prefix)).plus(
            TopHistoryExecutor.mamoeb.curses.take(10).map { ChatMessage("assistant", it) }).toMutableList()

    }
}