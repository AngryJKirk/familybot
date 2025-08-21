package dev.storozhenko.familybot.feature.download.executors

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.download.services.IgCookieService
import dev.storozhenko.familybot.feature.download.services.YtDlpWrapper
import dev.storozhenko.familybot.feature.settings.models.TikTokDownload
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.io.File
import java.time.Duration
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class TikTokDownloadExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig,
    private val cookieService: IgCookieService,
    private val ytDlpWrapper: YtDlpWrapper
) : Executor {
    private val log = KotlinLogging.logger { }
    private val okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(Duration.ofMinutes(1))
        .readTimeout(Duration.ofMinutes(1))
        .callTimeout(Duration.ofMinutes(1))
        .retryOnConnectionFailure(true)
        .build()

    override suspend fun execute(context: ExecutorContext) {
        val urls = getTikTokUrls(context)
        urls.forEach { url ->
            context.client.execute(SendChatAction(context.chat.idString, "upload_video"))
            val downloadedFile = download(url)
            val video = SendVideo
                .builder()
                .video(InputFile(downloadedFile))
                .chatId(context.chat.id)
                .replyToMessageId(context.message.messageId)
                .build()
            context.client.execute(video)
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
        val cookiePath = cookieService.getPath()
        val ig = isIG(url)
        val urlToDownload = if (ig) downloadIG(url) else url
        val file = ytDlpWrapper.downloadVideo(urlToDownload)
        if (file.exists().not() && ig && cookiePath != null) {
            log.info { "Falling back to running yt-dlp with cookies..." }
            return ytDlpWrapper.downloadVideo(url, "--cookies", cookiePath)
        }
        return file
    }

    private fun containsUrl(text: String): Boolean {
        return isIG(text) || isTikTok(text) || isVk(text) || isYtShort(text)
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isVk(text: String) = text.contains("vk.com/clip", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram.com", ignoreCase = true) && text.contains("/reel/", ignoreCase = true)

    private fun isYtShort(text: String) = text.contains("youtube.com/shorts/", ignoreCase = true)

    private fun downloadIG(url: String): String {
        val request = Request.Builder()
            .url(C.dc("l5VKR[9`b1E)N.yplMn<5+]{*T80x.PiWu9aU<B1m\$c%pE"))
            .header("Referer", C.dc("l5VKR[9`aI~uHy7plMn<Q[[j<R|\$qx!Q"))
            .header("Origin", C.dc("l5VKR[9`aI~uHy7plMn<Q[[j<R|\$qxO"))
            .header("url", encodeUrl(url))
            .build()
        return okHttpClient
            .newCall(request)
            .execute()
            .use { response ->
                val json = response.body?.string()
                log.info { "response: $json" }
                json?.parseJson<IGVideoResponse>()
            }?.video
            ?.firstOrNull()
            ?.video ?: return url
    }

    fun encodeUrl(text: String): String {
        val keyBytes = C.dc("{%_1[[.ey#L)vmml<UAJ").toByteArray()
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

object C {

    private const val ab =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\""

    fun dc(i: String): String {
        var o = ""

        val aI = mutableMapOf<Char, Int>()
        ab.forEachIndexed { x, it -> aI[it] = x }

        if (i != "") {
            val l = i.length
            var b = 0
            var s = 0
            var v = -1

            for (ix in 0..<l) {
                val nV = aI[i[ix]]
                if (v < 0) {
                    v = nV!!
                } else {
                    v += (nV!! * 91)
                    b = b or (v shl s)

                    s += if (v and 8191 > 88) 13 else 14

                    do {
                        o += ((b and 255).toChar())
                        b = b shr 8
                        s -= 8
                    } while (s > 7)
                    v = -1
                }
            }
            if (v + 1 > 0) {
                val a = b or (v shl s)

                o += ((a and 255).toChar())
            }
        }

        return o
    }
}