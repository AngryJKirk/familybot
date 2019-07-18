package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.pluralize
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.RawChatLogRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.Instant

@Component
class MeCommandExecutor(
    private val commonRepository: CommonRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val rawChatLogRepository: RawChatLogRepository,
    private val dictionary: Dictionary
) : CommandExecutor {
    override fun command(): Command {
        return Command.ME
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val user = update.toUser()
        val chat = update.toChat()
        val pidorCount = getPidorsCount(chat, user)
        val commandCount = getCommandCount(user)
        val messageCount = getMessageCount(chat, user)
        val message = setOf(pidorCount, commandCount, messageCount).joinToString("\n")

        return { it.send(update, message, replyToUpdate = true) }
    }

    private fun getMessageCount(chat: Chat, user: User): String {
        val messageCount = rawChatLogRepository.getMessageCount(chat, user)
        val word = pluralize(
            messageCount,
            dictionary.get(Phrase.PLURALIZED_MESSAGE_ONE),
            dictionary.get(Phrase.PLURALIZED_MESSAGE_FEW),
            dictionary.get(Phrase.PLURALIZED_MESSAGE_MANY)
        )
        return dictionary.get(Phrase.YOU_TALKED) + " $messageCount $word."
    }

    private fun getCommandCount(user: User): String {
        val commandCount =
            commandHistoryRepository.get(user, from = Instant.now().minusSeconds(60 * 60 * 24 * 3650)).size
        val word = pluralize(
            commandCount,
            dictionary.get(Phrase.PLURALIZED_COUNT_ONE),
            dictionary.get(Phrase.PLURALIZED_COUNT_FEW),
            dictionary.get(Phrase.PLURALIZED_COUNT_MANY)
        )
        return dictionary.get(Phrase.YOU_USED_COMMANDS) + " $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User): String {
        val pidorCount = commonRepository
            .getPidorsByChat(chat, startDate = Instant.now().minusSeconds(60 * 60 * 24 * 3650))
            .filter { it.user.id == user.id }
            .size
        val word = pluralize(
            pidorCount,
            dictionary.get(Phrase.PLURALIZED_COUNT_ONE),
            dictionary.get(Phrase.PLURALIZED_COUNT_FEW),
            dictionary.get(Phrase.PLURALIZED_COUNT_MANY)
        )
        return pidorCount
            .takeIf { it > 0 }
            ?.let { dictionary.get(Phrase.YOU_WAS_PIDOR) + " $it $word." }
            ?: dictionary.get(Phrase.YOU_WAS_NOT_PIDOR)
    }
}
