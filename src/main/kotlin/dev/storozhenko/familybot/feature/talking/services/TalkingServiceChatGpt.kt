package dev.storozhenko.familybot.feature.talking.services

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.github.benmanes.caffeine.cache.Caffeine
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.SenderLogger.log
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.settings.models.ChatGPTMemory
import dev.storozhenko.familybot.feature.settings.models.ChatGPTStyle
import dev.storozhenko.familybot.feature.settings.models.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.feature.settings.models.RagContext
import dev.storozhenko.familybot.feature.talking.services.rag.RagService
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.LinkedList
import kotlin.time.Duration.Companion.seconds

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    private val gptSettingsReader: GptSettingsReader,
    private val ragService: RagService,
    private val botConfig: BotConfig,
    private val aiService: AiService,
) : TalkingService {
    companion object {
        private const val SIZE_LIMITER =
            "В ответах говори исключительно в мужском роде. Отвечай коротко. Не используй markdown или html. "
        private val codeMarkupPattern = Regex("`{1,3}([^`]+)`{1,3}")
    }

    private val caches = GptStyle.entries
        .associateWith { _ ->
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .build<String, MutableList<ChatMessage>> { LinkedList() }
        }

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        var text = context.message.text ?: context.message.caption
        var photoDescription: String? = aiService.getImageDescription(context)
        if (photoDescription != null) {
            photoDescription =
                "<Пользователь отправил изображение, вот описание изображения, реагируй как если бы ты ее видел: ${
                    photoDescription
                }>"
        }
        val chatId = context.chat.idString
        if (text == "/reset") {
            caches.values.forEach { cache -> cache.invalidate(chatId) }
            return "OK"
        }

        val style = getStyle(context)
        val chatMessages = getPastMessages(style, context)
        val systemMessage = getSystemMessage(style, context, chatMessages)
        if (text == "/debug") {
            return chatMessages.plus(systemMessage).joinToString("\n", transform = ChatMessage::toString)
        }
        text = if (text == null && photoDescription == null) {
            "<пользователь скинул хрень>"
        } else if (text != null && photoDescription != null) {
            "$photoDescription ${context.user.name} говорит: $text"
        } else if (text == null && photoDescription != null) {
            "${context.user.name} отправил: $photoDescription"
        } else {
            "${context.user.name} говорит: $text"
        }
        chatMessages.add(ChatMessage(Role.User, content = text))
        chatMessages.add(0, systemMessage)
        val request = createRequest(chatMessages, useGpt4 = false)
        val response = getOpenAIService().chatCompletion(request)
        saveMetric(context, response)
        chatMessages.removeFirst()
        val message = response.choices.first().message
        val messageContent = message.content
            ?: throw FamilyBot.InternalException("Message content is null, response is $response")
        chatMessages.add(message)
        return fixFormat(messageContent)
    }

    suspend fun internalMessage(message: String, useGpt4: Boolean = false): String {
        try {
            if (botConfig.openAiToken == null) return "<ChatGPT is not available due to missing token>"
            val request = createRequest(mutableListOf(ChatMessage(Role.User, content = message)), useGpt4)
            val response = getOpenAIService().chatCompletion(request)
            return response.choices.first().message.content ?: "<ChatGPT response is not available>"
        } catch (e: Exception) {
            log.error(e) { "Internal message ChatGPT failure" }
            return "Internal message ChatGPT failure"
        }
    }

    private fun fixFormat(message: String) = message.replace(codeMarkupPattern, "$1".code())

    private fun getPastMessages(
        style: GptStyle,
        context: ExecutorContext,
    ): MutableList<ChatMessage> {

        val chatId = context.chat.idString
        val cache = caches[style] ?: throw FamilyBot.InternalException("Internal logic error, check logs")
        var chatMessages = cache.get(chatId)

        if (chatMessages.size >= 40) {
            cache.invalidate(chatId)
            chatMessages = cache.get(chatId)
        }

        return chatMessages
    }

    private suspend fun getSystemMessage(
        style: GptStyle,
        context: ExecutorContext,
        chatMessages: MutableList<ChatMessage>,
    ): ChatMessage {
        return ChatMessage(
            Role.System, content = listOf(
                gptSettingsReader.getUniverseValue(style.universe).trimIndent(),
                gptSettingsReader.getStyleValue(style),
                getRagMemory(context, chatMessages),
                getMemory(context),
                SIZE_LIMITER
            ).joinToString("\n")
        )
    }

    private fun createRequest(chatMessages: MutableList<ChatMessage>, useGpt4: Boolean): ChatCompletionRequest {
        val model = "x-ai/grok-4.1-fast"

        return ChatCompletionRequest(
            model = ModelId(model),
            messages = chatMessages
        )
    }

    private fun saveMetric(context: ExecutorContext, response: ChatCompletion) {
        val currentValue = easyKeyValueService.get(ChatGPTTokenUsageByChat, context.chatKey, 0)
        easyKeyValueService.put(
            ChatGPTTokenUsageByChat,
            context.chatKey,
            currentValue + (response.usage?.totalTokens ?: 0),
            untilNextMonth(),
        )
    }

    private fun getStyle(context: ExecutorContext): GptStyle {
        return GptStyle.lookUp(easyKeyValueService.get(ChatGPTStyle, context.chatKey, GptStyle.RUDE.value))
            ?: GptStyle.RUDE
    }

    private fun getMemory(context: ExecutorContext): String {
        val memoryValue = easyKeyValueService.get(ChatGPTMemory, context.chatKey) ?: return ""

        return "У тебя есть память о пользователях чата, применяй ее. Вот она: \n|НАЧАЛО ПАМЯТИ|\n$memoryValue\n|КОНЕЦ ПАМЯТИ|\n"
    }

    private suspend fun getRagMemory(context: ExecutorContext, chatMessages: MutableList<ChatMessage>): String {
        return if (easyKeyValueService.get(RagContext, context.chatKey, false)) {
            ragService.getContext(context, chatMessages)
        } else {
            ""
        }
    }

    private var openAI: OpenAI? = null

    private fun getOpenAIService(): OpenAI {
        if (openAI == null) {
            val token = botConfig.openAiToken
                ?: throw FamilyBot.InternalException("Open AI token is not available, check config")
            openAI = OpenAI(
                host = OpenAIHost(baseUrl = "https://openrouter.ai/api/v1/"),
                token = token,
                timeout = Timeout(socket = 60.seconds),
                logging = LoggingConfig(logLevel = LogLevel.None)
            )
        }
        return openAI as OpenAI
    }


}
