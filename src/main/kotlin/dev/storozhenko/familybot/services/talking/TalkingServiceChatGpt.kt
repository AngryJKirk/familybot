package dev.storozhenko.familybot.services.talking

import com.github.benmanes.caffeine.cache.Caffeine
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import dev.storozhenko.familybot.common.extensions.code
import dev.storozhenko.familybot.common.extensions.randomInt
import dev.storozhenko.familybot.common.extensions.startOfDay
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.settings.ChatGPTStyle
import dev.storozhenko.familybot.services.settings.ChatGPTTokenUsageByChat
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.LinkedList

@Component("GPT")
class TalkingServiceChatGpt(
    private val easyKeyValueService: EasyKeyValueService,
    private val gptSettingsReader: GptSettingsReader,
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : TalkingService {
    companion object {
        private val codeMarkupPattern = Regex("`{1,3}([^`]+)`{1,3}")
    }

    private val openAI = OpenAiService(botConfig.openAiToken, Duration.ofMinutes(2))

    private val caches = GptStyle
        .values()
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
        val style = getStyle(context)

        val chatMessages = getPastMessages(style, context)
        val systemMessage = getSystemMessage(style, context)
        if (text == "/debug") {
            return chatMessages.plus(systemMessage).joinToString("\n", transform = ChatMessage::toString)
        }
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
        chatMessages.add(0, systemMessage)
        val request = createRequest(chatMessages)
        val response = openAI.createChatCompletion(request)
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

    private fun fixFormat(message: String): String {
        return message.replace(codeMarkupPattern, "$1".code())
    }

    private fun getPastMessages(
        style: GptStyle,
        context: ExecutorContext
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
        style: GptStyle,
        context: ExecutorContext
    ): ChatMessage {

        val pidorMessage = getCurrentPidors(context)
        val universeValue = if (pidorMessage != null && style != GptStyle.ASSISTANT) {
            gptSettingsReader.getUniverseValue(style.universe) + pidorMessage
        } else {
            gptSettingsReader.getUniverseValue(style.universe)
        }
        return ChatMessage("system", universeValue.trimIndent())
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

    private fun getCurrentPidors(context: ExecutorContext): String? {
        if (easyKeyValueService.get(FunctionId.Pidor, context.chatKey, false).not()) {
            return null
        }
        val pidorsByChat = commonRepository.getPidorsByChat(context.chat, startDate = startOfDay())
        return if (pidorsByChat.isEmpty()) {
            null
        } else {
            val currentPidors = pidorsByChat.joinToString(", ") { it.user.getGeneralName(mention = true) }
            "\nСписок пидоров дня: $currentPidors."
        }
    }
}