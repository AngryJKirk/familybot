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
            return getOpenAIService().chatCompletion(
                ChatCompletionRequest(
                    model = ModelId("gpt-4o-mini"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.User,
                            content = listOf(
                                ImagePart(url),
                                TextPart("Пользователь отправил эту картинку. Опиши ее.")
                            )
                        )
                    )
                )
            ).choices.first().message.content ?: "Не получается описать картинку"
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

    private fun getOpenAIService(): OpenAI {
        if (openAI == null) {
            val token = botConfig.openAiToken
                ?: throw FamilyBot.InternalException("Open AI token is not available, check config")
            openAI = OpenAI(
                token = token,
                timeout = Timeout(socket = 60.seconds),
                logging = LoggingConfig(logLevel = LogLevel.None)
            )
        }
        return openAI as OpenAI
    }

}