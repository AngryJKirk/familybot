package dev.storozhenko.familybot.executors.pm

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.executors.eventbased.TikTokDownloadExecutor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.TikTokDownload

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