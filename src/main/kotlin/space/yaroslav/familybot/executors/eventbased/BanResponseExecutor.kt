package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.services.misc.BanService

@Component
class BanResponseExecutor(
    private val banService: BanService
) : Executor {

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val banMessage = banService.isChatBanned(executorContext.chat)
            ?: banService.isUserBanned(executorContext.user)
            ?: "иди нахуй"
        return {
            if (executorContext.command != null) {
                it.send(executorContext, banMessage, replyToUpdate = true)
            }
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        val chat = message.chat.toChat()
        val user = message.from.toUser(chat = chat)
        return banCheck(chat, user) != null
    }

    override fun priority(executorContext: ExecutorContext) = Priority.HIGH

    private fun banCheck(chat: Chat, user: User): String? {
        return banService.isUserBanned(user) ?: banService.isChatBanned(chat)
    }
}
