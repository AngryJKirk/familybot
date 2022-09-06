package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import space.yaroslav.familybot.executors.eventbased.TikTokDownloadExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TikTokDownload

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