package dev.storozhenko.familybot.feature.download.services

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

@Component
class YtDlpWrapper(private val botConfig: BotConfig) {
    private val log = getLogger()
    private val fileDeleteScope = CoroutineScope(Dispatchers.Default)

    fun downloadVideo(url: String, vararg params: String): File {
        return download("${UUID.randomUUID()}.%(ext)s", url, *params)
    }

    fun downloadAudio(url: String, vararg params: String): File {
        return download("%(title)s.%(ext)s", url, "-x", "--audio-format", "mp3", "--no-playlist", *params)
    }

    private fun download(filename: String, url: String, vararg params: String): File {
        log.info("Running yt-dlp...")
        val folderName = UUID.randomUUID().toString()
        val folder = File("/tmp", folderName).apply { mkdir() }
        val process =
            ProcessBuilder(botConfig.ytdlLocation, url, "-o", "${folder.absolutePath}/$filename", *params).start()
        process.inputStream.reader(Charsets.UTF_8).use { log.info(it.readText()) }
        process.waitFor()
        log.info("Finished running yt-dlp")
        val downloadedFile =
            folder.listFiles()?.firstOrNull() ?: throw FamilyBot.InternalException("yt-dlp failed to extract data")
        downloadedFile.deleteOnExit()
        fileDeleteScope.launch {
            runCatching {
                delay(5.minutes)
                downloadedFile.delete()
                log.info("${downloadedFile.absolutePath} is deleted")
            }.onFailure {
                log.error("Could not delete ${downloadedFile.absolutePath}", it)
            }
        }
        return downloadedFile
    }
}