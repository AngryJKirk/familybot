package dev.storozhenko.familybot.feature.talking.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.SenderLogger.log
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.startOfDay
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.settings.models.ChatGPTStyle
import dev.storozhenko.familybot.feature.settings.models.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.LinkedList

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    private val gptSettingsReader: GptSettingsReader,
    private val pidorRepository: PidorRepository,
    private val botConfig: BotConfig,
) : TalkingService {
    companion object {
        private val codeMarkupPattern = Regex("`{1,3}([^`]+)`{1,3}")
    }

    private val caches = GptStyle.entries
        .associateWith { _ ->
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .build<String, MutableList<ChatMessage>> { LinkedList() }
        }

    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
        val text = context.message.text ?: "Я скинул тупой медиафайл"
        val chatId = context.chat.idString
        if (text == "/reset") {
            caches.values.forEach { cache -> cache.invalidate(chatId) }
            return "OK"
        }
        val shouldUseGpt4 = shouldUseGpt4(context)

        val style = getStyle(context)
        val universe = if (shouldUseGpt4) {
            GptUniverse.GPT4
        } else {
            style.universe
        }
        val chatMessages = getPastMessages(style, context)
        val systemMessage = getSystemMessage(universe)
        if (text == "/debug") {
            return chatMessages.plus(systemMessage).joinToString("\n", transform = ChatMessage::toString)
        }
        if (style == GptStyle.ASSISTANT) {
            chatMessages.add(ChatMessage("user", text))
        } else {
            chatMessages.add(
                ChatMessage(
                    "user",
                    listOf(text, gptSettingsReader.getStyleValue(style), getMessageSizeLimiter()).joinToString("\n"),
                ),
            )
        }
        chatMessages.add(0, systemMessage)
        val request = createRequest(chatMessages, useGpt4 = shouldUseGpt4)
        val response = getOpenAIService().createChatCompletion(request)
        saveMetric(context, response)
        chatMessages.removeFirst()
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

    fun internalMessage(message: String, useGpt4: Boolean = false): String {
        try {
            if (botConfig.openAiToken == null) return "<ChatGPT is not available due to missing token>"
            val request = createRequest(mutableListOf(ChatMessage("user", message)), useGpt4)
            val response = getOpenAIService().createChatCompletion(request)
            return response.choices.first().message.content
        } catch (e: Exception) {
            log.error(e) { "Internal message ChatGPT failure" }
            return "Internal message ChatGPT failure"
        }
    }

    private fun shouldUseGpt4(context: ExecutorContext): Boolean {
//        val isEnabled = easyKeyValueService.get(ChatGPT4Enabled, context.chatKey, false)
//        if (isEnabled.not()) return false
//
//        val messagesUsed = easyKeyValueService.get(ChatGPT4MessagesDailyCounter, context.chatKey)
//        if (messagesUsed == null) {
//            easyKeyValueService.put(ChatGPT4MessagesDailyCounter, context.chatKey, 1, untilNextDay())
//            return true
//        } else {
//            if (messagesUsed > 30) {
//                return false
//            } else {
//                easyKeyValueService.increment(ChatGPT4MessagesDailyCounter, context.chatKey)
//                return true
//            }
//        }
        // TODO fix AI alignment
        return false
    }

    private fun fixFormat(message: String) = message.replace(codeMarkupPattern, "$1".code())

    private fun getPastMessages(
        style: GptStyle,
        context: ExecutorContext,
    ): MutableList<ChatMessage> {
        val chatId = context.chat.idString
        val cache = caches[style] ?: throw FamilyBot.InternalException("Internal logic error, check logs")
        var chatMessages = cache.get(chatId)

        if (chatMessages.size > 11) {
            cache.invalidate(chatId)
            chatMessages = cache.get(chatId)
        }

        return chatMessages
    }

    private fun getSystemMessage(
        universe: GptUniverse,
    ): ChatMessage {
        return ChatMessage("system", gptSettingsReader.getUniverseValue(universe).trimIndent())
    }

    private fun createRequest(chatMessages: MutableList<ChatMessage>, useGpt4: Boolean): ChatCompletionRequest {
        val model = if (useGpt4) {
            "gpt-4o"
        } else {
            "gpt-4o-mini"
        }
        return ChatCompletionRequest
            .builder()
            .model(model)
            .messages(chatMessages)
            .temperature(0.7)
            .topP(0.8)
            .frequencyPenalty(1.0)
            .presencePenalty(1.0)
            .build()
    }

    private fun saveMetric(context: ExecutorContext, response: ChatCompletionResult) {
        val currentValue = easyKeyValueService.get(ChatGPTTokenUsageByChat, context.chatKey, 0)
        easyKeyValueService.put(
            ChatGPTTokenUsageByChat,
            context.chatKey,
            currentValue + response.usage.totalTokens,
            untilNextMonth(),
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

    private fun getCurrentPidors(context: ExecutorContext): String? {
        if (easyKeyValueService.get(FunctionId.Pidor, context.chatKey, false).not()) {
            return null
        }
        val pidorsByChat = pidorRepository.getPidorsByChat(context.chat, startDate = startOfDay())
        return if (pidorsByChat.isEmpty()) {
            null
        } else {
            val currentPidors = pidorsByChat.joinToString(", ") { it.user.getGeneralName(mention = false) }
            "\nСписок пидоров дня: $currentPidors."
        }
    }

    private var openAI: OpenAiService? = null

    private fun getOpenAIService(): OpenAiService {
        if (openAI == null) {
            openAI = OpenAiService(botConfig.openAiToken, Duration.ofMinutes(2))
        }
        return openAI as OpenAiService
    }
}
