package space.yaroslav.familybot.executors.command

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.PluralizedWordsProvider
import space.yaroslav.familybot.common.utils.pluralize
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.RawChatLogRepository
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class MeCommandExecutor(
    private val commonRepository: CommonRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val rawChatLogRepository: RawChatLogRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config) {
    companion object {
        const val TEN_YEARS_AGO: Long = 60 * 60 * 24 * 3650
    }

    val cache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(
            CacheLoader.from { id: Pair<User, Chat>? ->
                runBlocking {
                    executeInternal(
                        id ?: throw FamilyBot.InternalException("None has been passed to cache function of /me")
                    )
                }
            }
        )

    override fun command(): Command {
        return Command.ME
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        val message = cache[update.toUser() to update.toChat()]
        return {
            it.send(update, message, replyToUpdate = true)
        }
    }

    private suspend fun executeInternal(id: Pair<User, Chat>): String {
        val (user, chat) = id
        val messageCount = GlobalScope.async { getMessageCount(chat, user) }
        val pidorCount = GlobalScope.async { getPidorsCount(chat, user) }
        val commandCount = GlobalScope.async { getCommandCount(user) }
        return setOf(pidorCount.await(), commandCount.await(), messageCount.await()).joinToString("\n")
    }

    private fun getMessageCount(chat: Chat, user: User): String {
        val messageCount = rawChatLogRepository.getMessageCount(chat, user)
        val word = pluralize(
            messageCount,
            PluralizedWordsProvider(
                one = { dictionary.get(Phrase.PLURALIZED_MESSAGE_ONE) },
                few = { dictionary.get(Phrase.PLURALIZED_MESSAGE_FEW) },
                many = { dictionary.get(Phrase.PLURALIZED_MESSAGE_MANY) }
            )
        )
        return dictionary.get(Phrase.YOU_TALKED) + " $messageCount $word."
    }

    private fun getCommandCount(user: User): String {
        val commandCount =
            commandHistoryRepository.get(user, from = Instant.now().minusSeconds(TEN_YEARS_AGO)).size
        val word = pluralize(
            commandCount,
            PluralizedWordsProvider(
                one = { dictionary.get(Phrase.PLURALIZED_COUNT_ONE) },
                few = { dictionary.get(Phrase.PLURALIZED_COUNT_FEW) },
                many = { dictionary.get(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return dictionary.get(Phrase.YOU_USED_COMMANDS) + " $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User): String {
        val pidorCount = commonRepository
            .getPidorsByChat(chat, startDate = Instant.now().minusSeconds(TEN_YEARS_AGO))
            .filter { it.user.id == user.id }
            .size
        val word = pluralize(
            pidorCount,
            PluralizedWordsProvider(
                one = { dictionary.get(Phrase.PLURALIZED_COUNT_ONE) },
                few = { dictionary.get(Phrase.PLURALIZED_COUNT_FEW) },
                many = { dictionary.get(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return pidorCount
            .takeIf { it > 0 }
            ?.let { dictionary.get(Phrase.YOU_WAS_PIDOR) + " $it $word." }
            ?: dictionary.get(Phrase.YOU_WAS_NOT_PIDOR)
    }
}
