package dev.storozhenko.familybot.executors.command.stats

import dev.storozhenko.familybot.common.extensions.DateConstants
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.pluralize
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.repos.CommandHistoryRepository
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.repos.RawChatLogRepository
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.MessageCounter
import dev.storozhenko.familybot.services.settings.UserAndChatEasyKey
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class MeCommandExecutor(
    private val commonRepository: CommonRepository,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val rawChatLogRepository: RawChatLogRepository,
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor() {

    override fun command(): Command {
        return Command.ME
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val user = context.user
        return {
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
            it.send(context, message, replyToUpdate = true)
        }
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
        val pidorCount = commonRepository
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
