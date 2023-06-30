package dev.storozhenko.familybot.feature.tiktok.executors

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.TikTokDownload
import dev.storozhenko.familybot.feature.tiktok.services.IgCookieService
import dev.storozhenko.familybot.getLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.io.File
import java.time.Duration
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class TikTokDownloadExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig,
    private val cookieService: IgCookieService,
) : Executor {
    private val log = getLogger()
    private val okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(Duration.ofMinutes(1))
        .readTimeout(Duration.ofMinutes(1))
        .callTimeout(Duration.ofMinutes(1))
        .retryOnConnectionFailure(true)
        .build()

    override suspend fun execute(context: ExecutorContext) {
        val urls = getTikTokUrls(context)
        urls.forEach { url ->
            context.sender.execute(SendChatAction(context.chat.idString, "upload_video", null))
            val downloadedFile = download(url)
            val video = SendVideo
                .builder()
                .video(InputFile(downloadedFile))
                .chatId(context.chat.id)
                .replyToMessageId(context.message.messageId)
                .build()
            context.sender.execute(video)
            downloadedFile.delete()
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return botConfig.ytdlLocation != null &&
            getTikTokUrls(context).isNotEmpty() &&
            easyKeyValueService.get(TikTokDownload, context.chatKey, false)
    }

    override fun priority(context: ExecutorContext) = Priority.VERY_LOW

    private fun getTikTokUrls(context: ExecutorContext): List<String> {
        return context
            .message
            .entities
            ?.filter { it.type == "url" }
            ?.mapNotNull { it.text }
            ?.filter(::containsUrl)
            ?: emptyList()
    }

    private fun download(url: String): File {
        val filename = "/tmp/${UUID.randomUUID()}.mp4"
        val cookiePath = cookieService.getPath()
        val ig = isIG(url)
        log.info("Running yt-dlp...")
        val process = if (ig) {
            ProcessBuilder(botConfig.ytdlLocation, downloadIG(url), "-o", filename).start()
        } else {
            ProcessBuilder(botConfig.ytdlLocation, url, "-o", filename).start()
        }
        process.inputStream.reader(Charsets.UTF_8).use {
            log.info(it.readText())
        }
        process.waitFor()
        log.info("Finished running yt-dlp")
        val file = File(filename)
        if (file.exists().not() && ig && cookiePath != null) {
            log.info("Falling back to running yt-dlp with cookies...")
            ProcessBuilder(botConfig.ytdlLocation, url, "-o", filename, "--cookies", cookiePath).start().waitFor()
        }
        return file
    }

    private fun containsUrl(text: String): Boolean {
        return isIG(text) || isTikTok(text)
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram.com/reel", ignoreCase = true)

    private fun downloadIG(url: String): String? {
        log.info("Using 3rd party service to obtain IG url")
        val request = Request.Builder()
            .url("https://backend.instavideosave.com/allinone")
            .header("Referer", "https://instavideosave.net/")
            .header("Origin", "https://instavideosave.net/")
            .header("url", encodeUrl(url))
            .build()
        return okHttpClient
            .newCall(request)
            .execute()
            .use { response ->
                val json = response.body?.string()
                log.info("3d party service response: $json")
                json?.parseJson<IGVideoResponse>()
            }?.video
            ?.firstOrNull()
            ?.video ?: return null
    }

    fun encodeUrl(text: String): String {
        val keyBytes = "qwertyuioplkjhgf".toByteArray()
        val textBytes = text.toByteArray()

        val paddingSize = 16 - (textBytes.size % 16)
        val paddedBytes = ByteArray(textBytes.size + paddingSize)
        System.arraycopy(textBytes, 0, paddedBytes, 0, textBytes.size)
        for (i in textBytes.size until paddedBytes.size) {
            paddedBytes[i] = paddingSize.toByte()
        }
        return Cipher.getInstance("AES/ECB/PKCS5Padding")
            .apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES")) }
            .doFinal(paddedBytes)
            .let(Hex::encodeHexString)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class IGVideoResponse(val video: List<IGVideo>)

@JsonIgnoreProperties(ignoreUnknown = true)
class IGVideo(val video: String)
