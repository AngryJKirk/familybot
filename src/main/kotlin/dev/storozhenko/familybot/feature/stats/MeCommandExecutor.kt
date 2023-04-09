package dev.storozhenko.familybot.feature.stats

import dev.storozhenko.familybot.common.extensions.DateConstants
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import dev.storozhenko.familybot.feature.logging.repos.RawChatLogRepository
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.settings.models.MessageCounter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class MeCommandExecutor(
    private val pidorRepository: PidorRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val rawChatLogRepository: RawChatLogRepository,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor() {

    override fun command(): Command {
        return Command.ME
    }

    override suspend fun execute(context: ExecutorContext) {
        val chat = context.chat
        val user = context.user
        val message = coroutineScope {
            val messageCount = async { getMessageCount(chat, user, context) }
            val pidorCount = async { getPidorsCount(chat, user, context) }
            val commandCount = async { getCommandCount(user, context) }
            setOf(
                pidorCount.await(),
                commandCount.await(),
                messageCount.await()
            ).joinToString("\n")
        }
        context.sender.send(context, message, replyToUpdate = true)
    }

    private fun getMessageCount(chat: Chat, user: User, context: ExecutorContext): String {
        val key = UserAndChatEasyKey(user.id, chat.id)
        val messageCounter = easyKeyValueService.get(MessageCounter, key)
            ?: rawChatLogRepository.getMessageCount(chat, user).toLong()
                .also { count -> easyKeyValueService.put(MessageCounter, key, count) }

        val word = pluralize(
            messageCounter,
            PluralizedWordsProvider(
                one = { context.phrase(Phrase.PLURALIZED_MESSAGE_ONE) },
                few = { context.phrase(Phrase.PLURALIZED_MESSAGE_FEW) },
                many = { context.phrase(Phrase.PLURALIZED_MESSAGE_MANY) }
            )
        )
        return context.phrase(Phrase.YOU_TALKED) + " $messageCounter $word."
    }

    private fun getCommandCount(user: User, context: ExecutorContext): String {
        val commandCount =
            commandHistoryRepository.get(user, from = DateConstants.theBirthDayOfFamilyBot).size
        val word = pluralize(
            commandCount,
            PluralizedWordsProvider(
                one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return context.phrase(Phrase.YOU_USED_COMMANDS) + " $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User, context: ExecutorContext): String {
        val pidorCount = pidorRepository
            .getPidorsByChat(chat, startDate = DateConstants.theBirthDayOfFamilyBot)
            .filter { (pidor) -> pidor.id == user.id }
            .size
        val word = pluralize(
            pidorCount,
            PluralizedWordsProvider(
                one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return pidorCount
            .takeIf { count -> count > 0 }
            ?.let { count -> context.phrase(Phrase.YOU_WAS_PIDOR) + " $count $word." }
            ?: context.phrase(Phrase.YOU_WAS_NOT_PIDOR)
    }
}
