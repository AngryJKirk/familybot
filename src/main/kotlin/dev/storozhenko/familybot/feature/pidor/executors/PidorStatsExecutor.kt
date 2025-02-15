package dev.storozhenko.familybot.feature.pidor.executors

import dev.storozhenko.familybot.common.extensions.PluralizedWordsProvider
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.formatTopList

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component

@Component
class PidorStatsExecutor(
    private val pidorRepository: PidorRepository,
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override suspend fun execute(context: ExecutorContext) {
        val chat = context.chat

        val pidorsByChat = pidorRepository.getPidorsByChat(chat)
            .map { it.user }
            .formatTopList(
                PluralizedWordsProvider(
                    one = { context.phrase(Phrase.PLURALIZED_COUNT_ONE) },
                    few = { context.phrase(Phrase.PLURALIZED_COUNT_FEW) },
                    many = { context.phrase(Phrase.PLURALIZED_COUNT_MANY) },
                ),
            )
            .take(100)
        val title = "${context.phrase(Phrase.PIDOR_STAT_ALL_TIME)}:\n".bold()
        context.send(title + pidorsByChat.joinToString("\n"), enableHtml = true)
    }

    override fun command(): Command {
        return Command.STATS_TOTAL
    }
}
