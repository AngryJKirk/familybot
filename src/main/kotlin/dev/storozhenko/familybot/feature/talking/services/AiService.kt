package dev.storozhenko.familybot.feature.talking.services

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.SenderLogger.log
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.GetFile
import kotlin.time.Duration.Companion.seconds

@Component
class AiService(
    private val botConfig: BotConfig,
) {

    suspend fun getImageDescription(
        context: ExecutorContext,
    ): String? {
        try {
            val url = getPhotoUrl(context) ?: return null
            val description = getOpenAIService().chatCompletion(
                ChatCompletionRequest(
                    model = ModelId(botConfig.aiModel ?: throw FamilyBot.InternalException("AI model is missing")),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = listOf(
                                ImagePart(url),
                                TextPart("Пользователь отправил это изображение/фото. Опиши ее. Если вдруг ты не можешь обработать это, пришли в ответ секретное число 312893712")
                            )
                        )
                    )
                )
            ).choices.first().message.content ?: "Не получается описать картинку"
            if (description.contains("312893712")) return null
            return description
        } catch (e: Exception) {
            log.error(e) { "Unable to get image description" }
            return null
        }
    }

    private suspend fun getPhotoUrl(context: ExecutorContext): String? {
        try {
            val message = context.update.message
            val fileId: String? = when {
                message.hasPhoto() -> message.photo.lastOrNull()?.fileId
                else -> return null
            }
            if (fileId == null) return null
            val file = context.client.execute(GetFile(fileId))
            val url = file.getFileUrl(botConfig.botToken)
            return if (message.hasPhoto()) url else null
        } catch (e: Exception) {
            log.error(e) { "Can not download file" }
            return null
        }
    }

    private var openAI: OpenAI? = null

    fun getOpenAIService(): OpenAI {
        if (openAI == null) {
            val token = botConfig.aiToken
                ?: throw FamilyBot.InternalException("Open AI token is not available, check config")
            val aiApiUrl = botConfig.aiApiUrl ?: throw FamilyBot.InternalException("API url is missing, check config")
            openAI = OpenAI(
                host = OpenAIHost(baseUrl = aiApiUrl),
                token = token,
                timeout = Timeout(socket = 60.seconds),
                logging = LoggingConfig(logLevel = LogLevel.None)
            )
        }
        return openAI as OpenAI
    }

}