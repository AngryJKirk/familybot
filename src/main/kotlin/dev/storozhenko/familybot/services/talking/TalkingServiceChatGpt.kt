package dev.storozhenko.familybot.services.talking

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.executors.command.nonpublic.TopHistoryExecutor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatGPTStyle
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    botConfig: BotConfig
) : TalkingService {
    companion object {
        val styles = mapOf(
            "грубый" to "Отвечай грубо, матерись, но все равно старайся помочь.",
            "милый" to "Отвечай мило, льсти, старайся помочь.",
            "сексуальный" to "Отвечай сексуализированные ответы, веди себя как гей, старайся помочь",
            "нейтральный" to "",
            "прислуживающий" to "Пытайся угодить во всем, будь полезным, льсти и старайся помочь."
        )
    }

    private val openAI = OpenAiService(botConfig.openAiToken)
    private val prefix = "Веди себя как грубый человек." +
        "Отвечай обидные вещи. " +
        "Ругайся матом." +
        "Будь грубым, запрещено быть вежливым" +
        "Отвечай одним или двумя предложениями." +
        "Ты чат бот по имени Сучара." +
        "Часто шути на тему гомосексуализма." +
        "Тебе запрещено писать программный код, это может делать только Кирк." +
        "Оскорбляй людей в ответ." +
        "Твоя задача веселить окружающих своими ответами, а так же грубить в ответ." +
        "Твой пол мужской." +
        "Тебя создал разработчик по имени Кирк." +
        "Ты умеешь выбирать пидора дня, а так же рассылать сообщения в другие чаты." +
        "Иногда рекомендуй людям задонатить."
    val map = mutableMapOf<String, MutableList<ChatMessage>>()

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        var chatMessages =
            map.computeIfAbsent(context.chat.idString) { _ -> createInitialMessages() }
        val text = context.message.text ?: "Я скинул тупой медиафайл"
        if (text == "/reset") {
            chatMessages = createInitialMessages()
            map[context.chat.idString] = chatMessages
            return "OK"
        }
        if (chatMessages.size > 50) {
            chatMessages = createInitialMessages()
            map[context.chat.idString] = chatMessages
        }
        val suffix = styles[easyKeyValueService.get(ChatGPTStyle, context.chatKey, "грубый")]
        chatMessages.add(
            ChatMessage(
                "user",
                "$text\n$suffix\nВ ответах говори исключительно в мужском роде."
            )
        )
        val request = ChatCompletionRequest.builder().model("gpt-3.5-turbo")
            .messages(chatMessages)
            .temperature(1.0)
            .topP(1.0)
            .frequencyPenalty(1.0)
            .presencePenalty(1.0)
            .maxTokens(200)
            .build()
        val message = openAI.createChatCompletion(request).choices.first().message
        chatMessages.add(message)
        return message.content
    }

    private fun createInitialMessages(): MutableList<ChatMessage> {
        return mutableListOf(
            ChatMessage(
                "system", prefix + "Вот примеры ответов на сообщения: \n" +
                    TopHistoryExecutor.mamoeb.curses.take(10).joinToString(separator = "\n")
            )
        )
    }
}