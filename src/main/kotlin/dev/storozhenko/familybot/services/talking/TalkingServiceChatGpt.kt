package dev.storozhenko.familybot.services.talking

import com.github.benmanes.caffeine.cache.Caffeine
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatGPTStyle
import dev.storozhenko.familybot.services.settings.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.stereotype.Component
import java.time.Duration

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    private val gptSettingsReader: GptSettingsReader,
    botConfig: BotConfig
) : TalkingService {
    companion object {
        private val codeMarkupPattern = Regex("`{1,3}([^`]+)`{1,3}")
    }

    private val openAI = OpenAiService(botConfig.openAiToken, Duration.ofMinutes(1))

    private val caches = GptStyle
        .values()
        .associateWith { style ->
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .build<String, MutableList<ChatMessage>> { createInitialMessages(style) }
        }


    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        val text = context.message.text ?: "Я скинул тупой медиафайл"
        val chatId = context.chat.idString
        if (text == "/reset") {
            caches.values.forEach { cache -> cache.invalidate(chatId) }
            return "OK"
        }
        val style = getStyle(context)

        val chatMessages = getPastMessages(style, chatId)

        if (style == GptStyle.ASSISTANT) {
            chatMessages.add(ChatMessage("user", text))
        } else {
            chatMessages.add(
                ChatMessage(
                    "user",
                    listOf(text, gptSettingsReader.getStyleValue(style), getMessageSizeLimiter()).joinToString("\n")
                )
            )
        }

        val request = createRequest(chatMessages)
        val response = openAI.createChatCompletion(request)
        saveMetric(context, response)
        val message = response.choices.first().message
        return if (style == GptStyle.ASSISTANT) {
            chatMessages.add(message)
            fixFormat(message.content)
        } else {
            chatMessages.removeLast()
            chatMessages.add(ChatMessage("user", text))
            chatMessages.add(message)
            message.content
        }
    }

    private fun fixFormat(message: String): String {
        return message.replace(codeMarkupPattern, "$1".code())
    }

    private fun getPastMessages(
        style: GptStyle,
        chatId: String
    ): MutableList<ChatMessage> {
        val cache = caches[style] ?: throw FamilyBot.InternalException("Internal logic error, check logs")
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

    private fun createInitialMessages(style: GptStyle): MutableList<ChatMessage> {
        return mutableListOf(
            ChatMessage(
                "system", gptSettingsReader.getUniverseValue(style.universe)
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

    private fun getStyle(context: ExecutorContext): GptStyle {
        return GptStyle.lookUp(easyKeyValueService.get(ChatGPTStyle, context.chatKey, GptStyle.RUDE.value))
            ?: GptStyle.RUDE
    }

    private fun getMessageSizeLimiter(): String {
        val randomInt = randomInt(10, 20)
        return "В ответах говори исключительно в мужском роде. Используй только $randomInt слов."
    }
}