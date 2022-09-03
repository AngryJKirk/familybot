package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TikTokDownload
import java.io.File
import java.util.UUID

@Component
class TikTokDownloadExecutor(
    private val easyKeyValueService: EasyKeyValueService
) : Executor {

    private val log = getLogger()

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val urls = getTikTokUrls(context)
        return {
            urls.forEach { url ->
                val downloadedFile = download(url)
                val video = SendVideo
                    .builder()
                    .video(InputFile(downloadedFile))
                    .chatId(context.chat.id)
                    .replyToMessageId(context.message.messageId)
                    .build()
                it.execute(video)
                downloadedFile.delete()
            }
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return getTikTokUrls(context).isNotEmpty() &&
            easyKeyValueService.get(TikTokDownload, context.chatKey, false)
    }

    override fun priority(context: ExecutorContext) = Priority.VERY_LOW

    private fun getTikTokUrls(context: ExecutorContext): List<String> {
        return context
            .message
            .entities
            ?.filter { it.type == "url" }
            ?.filter { it.text?.contains("tiktok", ignoreCase = true) ?: false }
            ?.map { it.text } ?: emptyList()
    }

    private fun download(url: String): File {
        val filename = "/tmp/${UUID.randomUUID()}.mp4"
        val process = ProcessBuilder("/usr/local/bin/yt-dlp", url, "-o", filename).start()
        log.info("Running yt-dlp...")
        process.inputStream.reader(Charsets.UTF_8).use {
            log.info(it.readText())
        }
        process.waitFor()
        log.info("Finished running yt-dlp")
        return File(filename)
    }
}