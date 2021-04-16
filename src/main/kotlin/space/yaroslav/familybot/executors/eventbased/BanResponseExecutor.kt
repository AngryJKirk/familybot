package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.misc.Ban
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class BanResponseExecutor(private val banService: BanService) : Executor {

    val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val ban = banService.isChatBanned(update.toChat()) ?: banService.isUserBanned(update.toUser())
            ?: throw FamilyBot.InternalException("Some logic mistake: executor should not be chosen in case of there are no ban")

        return {
            it.send(
                update,
                "Бан нахуй по причине \"${ban.description}\" до ${dateTimeFormatter.format(ban.till)}",
                replyToUpdate = true
            )
        }
    }

    override fun canExecute(message: Message): Boolean {
        val chat = message.chat.toChat()
        val user = message.from.toUser(chat = chat)
        return banCheck(chat, user) != null && isCommand(message)
    }

    override fun priority(update: Update) = Priority.HIGH

    private fun banCheck(chat: Chat, user: User): Ban? {
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
