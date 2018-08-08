package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.pluralize
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.RawChatLogRepository
import space.yaroslav.familybot.route.models.Command
import java.time.Instant

@Component
class MeCommandExecutor(
    val commonRepository: CommonRepository,
    val commandHistoryRepository: CommandHistoryRepository,
    val rawChatLogRepository: RawChatLogRepository
) : CommandExecutor {
    override fun command(): Command {
        return Command.ME
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val user = update.toUser()
        val chat = update.toChat()
        val pidorCount = getPidorsCount(chat, user)
        val commandCount =
            getCommandCount(user)
        val messageCount = getMessageCount(chat, user)

        return {
            it.execute(
                SendMessage(chat.id, setOf(pidorCount, commandCount, messageCount).joinToString("\n"))
                    .setReplyToMessageId(update.message.messageId)
            )
        }
    }

    private fun getMessageCount(chat: Chat, user: User): String {
        val messageCount = rawChatLogRepository.getMessageCount(chat, user)
        val word = pluralize(messageCount, "сообщение", "сообщения", "сообщений")
        return "Ты напиздел $messageCount $word."
    }

    private fun getCommandCount(user: User): String {
        val commandCount =
            commandHistoryRepository.get(user, from = Instant.now().minusSeconds(60 * 60 * 24 * 3650)).size
        val word = pluralize(commandCount, "раз", "раза", "раз")
        return "Ты использовал команды $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User): String {
        val pidorCount = commonRepository
            .getPidorsByChat(chat, startDate = Instant.now().minusSeconds(60 * 60 * 24 * 3650))
            .filter { it.user.id == user.id }
            .size
        val word = pluralize(pidorCount, "раз", "раза", "раз")
        return pidorCount
            .takeIf { it > 0 }
            ?.let { "Ты был пидором $it $word." }
            ?: "Ты не был пидором ни разу. Пидор."
    }
}
