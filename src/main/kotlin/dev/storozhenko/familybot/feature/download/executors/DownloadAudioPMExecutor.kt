package dev.storozhenko.familybot.feature.download.executors

import dev.storozhenko.familybot.core.executors.PrivateMessageExecutor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import org.springframework.stereotype.Component

@Component
class DownloadAudioPMExecutor(private val downloadAudioExecutor: DownloadAudioExecutor) : PrivateMessageExecutor {

    override suspend fun execute(context: ExecutorContext) = downloadAudioExecutor.execute(context)

    override fun canExecute(context: ExecutorContext) = downloadAudioExecutor.canExecute(context)

    override fun priority(context: ExecutorContext) = Priority.HIGH

}