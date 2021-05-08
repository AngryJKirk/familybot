package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class BanResponseExecutor(private val banService: BanService) : Executor {

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val banMessage = banService.isChatBanned(update.toChat()) ?: banService.isUserBanned(update.toUser())
        ?: throw FamilyBot.InternalException("Some logic mistake: executor should not be chosen in case of there are no ban")
        if (isCommand(update.message)) {
            return {
                it.send(
                    update,
                    banMessage,
                    replyToUpdate = true
                )
            }
        } else {
            return {}
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

    private fun isCommand(message: Message): Boolean {
        val text = message.text ?: ""
        return Command.values().map { it.command }
            .any { command ->
                text.startsWith("$command@") ||
                    text == command ||
                    text.startsWith("$command ")
            }
    }
}
