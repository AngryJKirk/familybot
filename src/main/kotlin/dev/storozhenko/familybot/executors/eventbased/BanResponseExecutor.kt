package dev.storozhenko.familybot.executors.eventbased

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.Executor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.services.misc.BanService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class BanResponseExecutor(
    private val banService: BanService
) : Executor {

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val banMessage = banService.getChatBan(context)
            ?: banService.getUserBan(context)
            ?: "иди нахуй"
        return {
            if (context.command != null) {
                it.send(context, banMessage, replyToUpdate = true)
            }
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return (banService.getUserBan(context) ?: banService.getChatBan(context)) != null
    }

    override fun priority(context: ExecutorContext) = Priority.HIGH
}
