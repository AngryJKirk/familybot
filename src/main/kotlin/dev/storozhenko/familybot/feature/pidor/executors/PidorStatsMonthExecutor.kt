package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.common.extensions.*
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import dev.storozhenko.familybot.feature.pidor.repos.CommonRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.LocalDate

@Component
class PidorStatsMonthExecutor(
    private val repository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_MONTH
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val now = LocalDate.now()

        val pidorsByChat = repository.getPidorsByChat(
            context.chat,
            startDate = startOfCurrentMonth()
        )
            .map(Pidor::user)
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) }
                )
            )
        val title = "${context.phrase(Phrase.PIDOR_STAT_MONTH)} ${now.month.toRussian()}:\n".bold()
        return { it.send(context, title + pidorsByChat.joinToString("\n"), enableHtml = true) }
    }
}
