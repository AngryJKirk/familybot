package dev.storozhenko.familybot.feature.talking.services

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.Effort
import com.aallam.openai.api.chat.ListContent
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.model.ModelId
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.LinkedList

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    private val gptSettingsReader: GptSettingsReader,
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
        val imagePart = coroutineScope { async { aiService.getImagePart(context) } }
        val chatId = context.chat.idString
        if (text == "/reset") {
            caches.values.forEach { cache -> cache.invalidate(chatId) }
            return "OK"
        }

        val style = getStyle(context)
        val chatMessages = getPastMessages(style, context)
        val systemMessage = getSystemMessage(style, context)
        text = "${context.user.name} говорит, отвечай ему в первом лице: $text"

        chatMessages.add(
            ChatMessage(
                role = Role.User,
                messageContent = ListContent(listOfNotNull(TextPart(text), imagePart.await()))
            )
        )
        chatMessages.add(0, systemMessage)
        val request = createRequest(chatMessages)
        val response = aiService.getOpenAIService().chatCompletion(request)
        saveMetric(context, response)
        chatMessages.removeFirst()
        val message = response.choices.first().message
        val messageContent = message.content
            ?: throw FamilyBot.InternalException("Message content is null, response is $response")
        chatMessages.add(message)
        return fixFormat(messageContent)
    }

    suspend fun internalMessage(message: String): String {
        try {
            if (botConfig.aiToken == null) return "<ChatGPT is not available due to missing token>"
            val request = createRequest(mutableListOf(ChatMessage(Role.User, content = message)))
            val response = aiService.getOpenAIService().chatCompletion(request)
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
        context: ExecutorContext
    ): ChatMessage {
        return ChatMessage(
            Role.System, content = listOf(
                gptSettingsReader.getUniverseValue(style.universe).trimIndent(),
                gptSettingsReader.getStyleValue(style),
                getMemory(context),
                SIZE_LIMITER
            ).joinToString("\n")
        )
    }

    private fun createRequest(chatMessages: MutableList<ChatMessage>): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = ModelId(botConfig.aiModel ?: throw FamilyBot.InternalException("Missing ai model, check config")),
            messages = chatMessages,
            reasoningEffort = Effort("none")
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



}
