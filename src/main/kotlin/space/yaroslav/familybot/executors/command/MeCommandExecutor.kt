package space.yaroslav.familybot.executors.command

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.pluralize
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.repos.RawChatLogRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
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

    val cache: LoadingCache<Pair<User, Chat>?, String> = CacheBuilder
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
        val context = dictionary.createContext(chat)
        return coroutineScope {
            val messageCount = async { getMessageCount(chat, user, context) }
            val pidorCount = async { getPidorsCount(chat, user, context) }
            val commandCount = async { getCommandCount(user, context) }
            setOf(
                pidorCount.await(),
                commandCount.await(),
                messageCount.await()
            ).joinToString("\n")
        }
    }

    private fun getMessageCount(chat: Chat, user: User, context: DictionaryContext): String {
        val messageCount = rawChatLogRepository.getMessageCount(chat, user)
        val word = pluralize(
            messageCount,
            PluralizedWordsProvider(
                one = { context.get(Phrase.PLURALIZED_MESSAGE_ONE) },
                few = { context.get(Phrase.PLURALIZED_MESSAGE_FEW) },
                many = { context.get(Phrase.PLURALIZED_MESSAGE_MANY) }
            )
        )
        return context.get(Phrase.YOU_TALKED) + " $messageCount $word."
    }

    private fun getCommandCount(user: User, context: DictionaryContext): String {
        val commandCount =
            commandHistoryRepository.get(user, from = Instant.now().minusSeconds(TEN_YEARS_AGO)).size
        val word = pluralize(
            commandCount,
            PluralizedWordsProvider(
                one = { context.get(Phrase.PLURALIZED_COUNT_ONE) },
                few = { context.get(Phrase.PLURALIZED_COUNT_FEW) },
                many = { context.get(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return context.get(Phrase.YOU_USED_COMMANDS) + " $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User, context: DictionaryContext): String {
        val pidorCount = commonRepository
            .getPidorsByChat(chat, startDate = Instant.now().minusSeconds(TEN_YEARS_AGO))
            .filter { it.user.id == user.id }
            .size
        val word = pluralize(
            pidorCount,
            PluralizedWordsProvider(
                one = { context.get(Phrase.PLURALIZED_COUNT_ONE) },
                few = { context.get(Phrase.PLURALIZED_COUNT_FEW) },
                many = { context.get(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return pidorCount
            .takeIf { it > 0 }
            ?.let { context.get(Phrase.YOU_WAS_PIDOR) + " $it $word." }
            ?: context.get(Phrase.YOU_WAS_NOT_PIDOR)
    }
}
