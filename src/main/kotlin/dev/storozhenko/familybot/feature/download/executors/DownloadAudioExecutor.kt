package dev.storozhenko.familybot.feature.download.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.download.services.YtDlpWrapper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendAudio
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.objects.InputFile

@Component
class DownloadAudioExecutor(
    private val botConfig: BotConfig,
    private val ytDlpWrapper: YtDlpWrapper
) : CommandExecutor() {
    private val spaceRegex = "\\s".toRegex()
    override fun command() = Command.AUDIO

    override suspend fun execute(context: ExecutorContext) {
        if (botConfig.ytdlLocation == null) {
            return
        }
        val split = context.message.text.split(spaceRegex)
        if (split.size != 2) {
            return
        }
        context.client.execute(SendChatAction(context.chat.idString, "upload_document"))
        val audio = ytDlpWrapper.downloadAudio(split[1])
        context.client.execute(
            SendAudio
                .builder()
                .audio(InputFile(audio))
                .chatId(context.chat.id)
                .replyToMessageId(context.message.messageId)
                .build()
        )

    }
}