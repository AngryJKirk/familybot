package dev.storozhenko.familybot.feature.tiktok.executors

import dev.storozhenko.familybot.core.executors.PrivateMessageExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.TikTokDownload
import org.springframework.stereotype.Component

@Component
class TikTokDownloadPMExecutor(
    private val tikTokDownloadExecutor: TikTokDownloadExecutor,
    private val easyKeyValueService: EasyKeyValueService
) : PrivateMessageExecutor {
    override fun execute(context: ExecutorContext) = tikTokDownloadExecutor.execute(context)

    override fun canExecute(context: ExecutorContext): Boolean {
        easyKeyValueService.put(TikTokDownload, context.chatKey, true)
        return tikTokDownloadExecutor.canExecute(context)
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH
}
