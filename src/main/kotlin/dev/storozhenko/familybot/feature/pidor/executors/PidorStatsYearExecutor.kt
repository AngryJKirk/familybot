package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.startOfTheYear
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PidorStatsYearExecutor(
    private val pidorRepository: PidorRepository,
) : CommandExecutor(), Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.STATS_YEAR
    }

    override suspend fun execute(context: ExecutorContext) {
        val now = LocalDate.now()
        val pidorsByChat = pidorRepository.getPidorsByChat(
            context.chat,
            startDate = startOfTheYear(),
        )
            .map(Pidor::user)
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) },
                ),
            )
        val title = "${context.phrase(Phrase.PIDOR_STAT_YEAR)} ${now.year}:\n".bold()
        context.sender.send(context, title + pidorsByChat.joinToString("\n"), enableHtml = true)
    }
}
