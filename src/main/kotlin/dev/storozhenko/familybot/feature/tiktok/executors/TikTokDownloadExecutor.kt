package dev.storozhenko.familybot.feature.tiktok.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.TikTokDownload
import dev.storozhenko.familybot.feature.tiktok.services.IgCookieService
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import java.io.File
import java.util.UUID

@Component
class TikTokDownloadExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig,
    private val cookieService: IgCookieService
) : Executor {
    private val log = getLogger()

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
        val process = if (isIG(url) && cookiePath != null) {
            log.info("Running yt-dlp with cookies...")
            ProcessBuilder(botConfig.ytdlLocation, url, "-o", filename, "--cookies", cookiePath).start()
        } else {
            log.info("Running yt-dlp...")
            ProcessBuilder(botConfig.ytdlLocation, url, "-o", filename).start()
        }
        process.inputStream.reader(Charsets.UTF_8).use {
            log.info(it.readText())
        }
        process.waitFor()
        log.info("Finished running yt-dlp")
        return File(filename)
    }

    private fun containsUrl(text: String): Boolean {
        return isIG(text) || isTikTok(text)
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram.com/reel", ignoreCase = true)
}
