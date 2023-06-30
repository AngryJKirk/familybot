package dev.storozhenko.familybot.feature.ban.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.ban.services.BanService
import org.springframework.stereotype.Component

@Component
class BanResponseExecutor(
    private val banService: BanService,
) : Executor {

    override suspend fun execute(context: ExecutorContext) {
        val banMessage = banService.getChatBan(context)
            ?: banService.getUserBan(context)
            ?: "иди нахуй"

        if (context.command != null) {
            context.sender.send(context, banMessage, replyToUpdate = true)
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return (banService.getUserBan(context) ?: banService.getChatBan(context)) != null
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH
}
