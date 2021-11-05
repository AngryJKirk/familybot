package space.yaroslav.familybot.executors.command.stats

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.DateConstants
import space.yaroslav.familybot.common.extensions.PluralizedWordsProvider
import space.yaroslav.familybot.common.extensions.pluralize
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.repos.RawChatLogRepository
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.MessageCounter
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey

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

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat
        val user = executorContext.user
        return {
            val message = coroutineScope {
                val messageCount = async { getMessageCount(chat, user, executorContext) }
                val pidorCount = async { getPidorsCount(chat, user, executorContext) }
                val commandCount = async { getCommandCount(user, executorContext) }
                setOf(
                    pidorCount.await(),
                    commandCount.await(),
                    messageCount.await()
                ).joinToString("\n")
            }
            it.send(executorContext, message, replyToUpdate = true)
        }
    }

    private fun getMessageCount(chat: Chat, user: User, executorContext: ExecutorContext): String {
        val key = UserAndChatEasyKey(user.id, chat.id)
        val messageCounter = easyKeyValueService.get(MessageCounter, key)
            ?: rawChatLogRepository.getMessageCount(chat, user).toLong()
                .also { count -> easyKeyValueService.put(MessageCounter, key, count) }

        val word = pluralize(
            messageCounter,
            PluralizedWordsProvider(
                one = { executorContext.phrase(Phrase.PLURALIZED_MESSAGE_ONE) },
                few = { executorContext.phrase(Phrase.PLURALIZED_MESSAGE_FEW) },
                many = { executorContext.phrase(Phrase.PLURALIZED_MESSAGE_MANY) }
            )
        )
        return executorContext.phrase(Phrase.YOU_TALKED) + " $messageCounter $word."
    }

    private fun getCommandCount(user: User, executorContext: ExecutorContext): String {
        val commandCount =
            commandHistoryRepository.get(user, from = DateConstants.theBirthDayOfFamilyBot).size
        val word = pluralize(
            commandCount,
            PluralizedWordsProvider(
                one = { executorContext.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                few = { executorContext.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                many = { executorContext.phrase(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return executorContext.phrase(Phrase.YOU_USED_COMMANDS) + " $commandCount $word."
    }

    private fun getPidorsCount(chat: Chat, user: User, executorContext: ExecutorContext): String {
        val pidorCount = commonRepository
            .getPidorsByChat(chat, startDate = DateConstants.theBirthDayOfFamilyBot)
            .filter { (pidor) -> pidor.id == user.id }
            .size
        val word = pluralize(
            pidorCount,
            PluralizedWordsProvider(
                one = { executorContext.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                few = { executorContext.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                many = { executorContext.phrase(Phrase.PLURALIZED_COUNT_MANY) }
            )
        )
        return pidorCount
            .takeIf { count -> count > 0 }
            ?.let { count -> executorContext.phrase(Phrase.YOU_WAS_PIDOR) + " $count $word." }
            ?: executorContext.phrase(Phrase.YOU_WAS_NOT_PIDOR)
    }
}
