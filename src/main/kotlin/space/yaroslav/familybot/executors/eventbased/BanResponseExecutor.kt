package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getCommand
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class BanResponseExecutor(
    private val botConfig: BotConfig,
    private val banService: BanService
) : Executor {

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val banMessage = banService.isChatBanned(update.toChat())
            ?: banService.isUserBanned(update.toUser())
            ?: "иди нахуй"
        return {
            if (isCommand(update.message)) {
                it.send(update, banMessage, replyToUpdate = true)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        val chat = message.chat.toChat()
        val user = message.from.toUser(chat = chat)
        return banCheck(chat, user) != null
    }

    override fun priority(update: Update) = Priority.HIGH

    private fun banCheck(chat: Chat, user: User): String? {
        return banService.isUserBanned(user) ?: banService.isChatBanned(chat)
    }

    private fun isCommand(message: Message) = message.getCommand(botConfig.botName) != null
}
