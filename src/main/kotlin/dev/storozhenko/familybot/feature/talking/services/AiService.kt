package dev.storozhenko.familybot.feature.talking.services

import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.SenderLogger.log
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.GetFile
import java.time.Duration
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.seconds

@Component
class AiService(
    private val botConfig: BotConfig,
) {

    private val client = OkHttpClient().newBuilder()
        .connectTimeout(Duration.ofMinutes(1))
        .readTimeout(Duration.ofMinutes(1))
        .callTimeout(Duration.ofMinutes(1))
        .retryOnConnectionFailure(true)
        .build()

    suspend fun getImagePart(
        context: ExecutorContext,
    ): ImagePart? {
        try {
            val base64EncodedImage = getPhotoUrl(context) ?: return null
            return ImagePart(url = base64EncodedImage)
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
            val photoUrl = if (message.hasPhoto()) url else return null
            return downloadImageAsBase64(photoUrl)
        } catch (e: Exception) {
            log.error(e) { "Can not download file" }
            return null
        }
    }

    fun downloadImageAsBase64(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val imageBytes = response.body?.bytes() ?: return null
            return "data:image/jpeg;base64," + Base64.encode(imageBytes)
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