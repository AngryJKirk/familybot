package dev.storozhenko.familybot.executors.command.stats

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.capitalized
import dev.storozhenko.familybot.common.extensions.dropLastDelimiter
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.startOfCurrentMonth
import dev.storozhenko.familybot.common.extensions.toRussian
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.dictionary.Pluralization
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.Pidor
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.telegram.FamilyBot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class TopPidorsByMonthsExecutor(
    private val commonRepository: CommonRepository
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    private class PidorStat(val user: User, val position: Int)

    private val delimiter = "\n========================\n"

    override fun command(): Command {
        return Command.LEADERBOARD
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {

        val result = commonRepository
            .getPidorsByChat(context.chat)
            .filter { it.date.isBefore(startOfCurrentMonth()) }
            .groupBy { map(it.date) }
            .mapValues { monthPidors -> calculateStats(monthPidors.value) }
            .toSortedMap()
            .asIterable()
            .reversed()
            .map(formatLeaderBoard(context))
        if (result.isEmpty()) {
            return {
                it.send(context, context.phrase(Phrase.LEADERBOARD_NONE))
            }
        }
        val message = "${context.phrase(Phrase.LEADERBOARD_TITLE)}:\n".bold()
        return {
            it.send(context, message + "\n" + result.joinToString(delimiter), enableHtml = true)
        }
    }

    private fun formatLeaderBoard(context: ExecutorContext): (Map.Entry<LocalDate, PidorStat>) -> String = {
        val month = it.key.month.toRussian().capitalized()
        val year = it.key.year
        val userName = it.value.user.name.dropLastDelimiter()
        val position = it.value.position
        val leaderboardPhrase = getLeaderboardPhrase(
            Pluralization.getPlur(it.value.position), context
        )
        "$month, $year:\n".italic() + "$userName, $position $leaderboardPhrase"
    }

    private fun map(instant: Instant): LocalDate {
        val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return LocalDate.of(time.year, time.month, 1)
    }

    private fun calculateStats(pidors: List<Pidor>): PidorStat {
        val pidor = pidors
            .groupBy { it.user }
            .maxByOrNull { it.value.size }
            ?: throw FamilyBot.InternalException("List of pidors should be not empty to calculate stats")
        return PidorStat(pidor.key, pidors.count { it.user == pidor.key })
    }

    private fun getLeaderboardPhrase(pluralization: Pluralization, context: ExecutorContext): String {
        return when (pluralization) {
            Pluralization.ONE -> context.phrase(Phrase.PLURALIZED_LEADERBOARD_ONE)
            Pluralization.FEW -> context.phrase(Phrase.PLURALIZED_LEADERBOARD_FEW)
            Pluralization.MANY -> context.phrase(Phrase.PLURALIZED_LEADERBOARD_MANY)
        }
    }
}
