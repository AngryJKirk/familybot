package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.misc.BanService

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
