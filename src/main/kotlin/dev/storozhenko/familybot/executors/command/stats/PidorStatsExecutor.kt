package dev.storozhenko.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.repos.CommonRepository

@Component
class PidorStatsExecutor(
    private val repository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat

        val pidorsByChat = repository.getPidorsByChat(chat)
            .map { it.user }
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
            .take(100)
        val title = "${context.phrase(Phrase.PIDOR_STAT_ALL_TIME)}:\n".bold()
        return {
            it.send(context, title + pidorsByChat.joinToString("\n"), enableHtml = true)
        }
    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }
}
