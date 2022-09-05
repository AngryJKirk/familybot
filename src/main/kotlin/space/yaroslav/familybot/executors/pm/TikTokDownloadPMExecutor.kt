package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import space.yaroslav.familybot.executors.eventbased.TikTokDownloadExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority

@Component
class TikTokDownloadPMExecutor(
    private val tikTokDownloadExecutor: TikTokDownloadExecutor
) : PrivateMessageExecutor {
    override fun execute(context: ExecutorContext) = tikTokDownloadExecutor.execute(context)

    override fun canExecute(context: ExecutorContext) = tikTokDownloadExecutor.canExecute(context)

    override fun priority(context: ExecutorContext) = Priority.HIGH
}