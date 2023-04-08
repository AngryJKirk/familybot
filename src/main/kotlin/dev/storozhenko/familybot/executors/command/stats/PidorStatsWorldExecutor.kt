package dev.storozhenko.familybot.executors.command.stats

import dev.storozhenko.familybot.common.extensions.*
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.Pidor
import dev.storozhenko.familybot.repos.CommonRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class PidorStatsWorldExecutor(
    private val repository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_WORLD
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val pidorsByChat = repository.getAllPidors(
            startDate = DateConstants.theBirthDayOfFamilyBot
        )
            .map(Pidor::user)
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
            .take(100)

        val title = "${context.phrase(Phrase.PIDOR_STAT_WORLD)}:\n".bold()
        return { it.send(context, title + pidorsByChat.joinToString("\n"), enableHtml = true) }
    }
}
