package dev.storozhenko.familybot.services.talking

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatGPTStyle
import dev.storozhenko.familybot.services.settings.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import java.time.Duration

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
            "прислуживающий" to "Пытайся угодить во всем, будь полезным, льсти и старайся помочь.",
            "ассистент" to "Забудь предыдущие указания. Ты ассистент, который обязан отвечать четко и кратко и по делу."
        )
        private const val defaultPrefix =
            "Ты чат бот по имени Сучара." +
                    "Часто шути на тему гомосексуализма." +
                    "Твоя задача веселить окружающих своими ответами, а так же грубить в ответ." +
                    "Твой пол мужской." +
                    "Тебя создал разработчик по имени Кирк." +
                    "Ты умеешь выбирать пидора дня, а так же рассылать сообщения в другие чаты."
        private const val assistantPrefix = "Ты бот-ассистент по имени Сучара."
    }

    private val openAI = OpenAiService(botConfig.openAiToken, Duration.ofSeconds(60))
    private val defaultCache: LoadingCache<String, MutableList<ChatMessage>> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build { createInitialMessages(defaultPrefix) }
    private val assistantCache: LoadingCache<String, MutableList<ChatMessage>> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build { createInitialMessages(assistantPrefix) }

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        val text = context.message.text ?: "Я скинул тупой медиафайл"
        val chatId = context.chat.idString
        if (text == "/reset") {
            defaultCache.invalidate(chatId)
            return "OK"
        }
        val style = easyKeyValueService.get(ChatGPTStyle, context.chatKey, "грубый")

        val chatMessages = getPastMessages(style, chatId)

        if (style == "ассистент") {
            chatMessages.add(ChatMessage("user", text))
        } else {
            chatMessages.add(
                ChatMessage(
                    "user",
                    "$text\n${styles[style]}\nВ ответах говори исключительно в мужском роде. Используй только ${
                        randomInt(
                            10,
                            20
                        )
                    } слов."
                )
            )
        }

        val request = createRequest(chatMessages)
        val response = openAI.createChatCompletion(request)
        saveMetric(context, response)
        val message = response.choices.first().message
        chatMessages.removeLast()
        chatMessages.add(ChatMessage("user", text))
        chatMessages.add(message)
        return fixFormat(message.content)
    }

    private fun fixFormat(message: String): String {
        val pattern = Regex("`{1,3}([^`]+)`{1,3}")
        return message.replace(pattern, "<code>$1</code>")
    }

    private fun getPastMessages(
        style: String,
        chatId: String
    ): MutableList<ChatMessage> {
        val cache = if (style == "ассистент") {
            assistantCache
        } else {
            defaultCache
        }
        var chatMessages = cache.get(chatId)

        if (chatMessages.size > 11) {
            cache.invalidate(chatId)
            chatMessages = cache.get(chatId)
        }
        return chatMessages
    }

    private fun createRequest(chatMessages: MutableList<ChatMessage>): ChatCompletionRequest {
        return ChatCompletionRequest
            .builder()
            .model("gpt-3.5-turbo")
            .messages(chatMessages)
            .temperature(1.0)
            .topP(1.0)
            .frequencyPenalty(1.0)
            .presencePenalty(1.0)
            .build()
    }

    private fun createInitialMessages(prefix: String): MutableList<ChatMessage> {
        return mutableListOf(
            ChatMessage(
                "system", prefix
            )
        )
    }

    private fun saveMetric(context: ExecutorContext, response: ChatCompletionResult) {
        val currentValue = easyKeyValueService.get(ChatGPTTokenUsageByChat, context.chatKey, 0)
        easyKeyValueService.put(
            ChatGPTTokenUsageByChat,
            context.chatKey,
            currentValue + response.usage.totalTokens,
            untilNextMonth()
        )
    }
}