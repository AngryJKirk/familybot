package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toRussian
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class TopPidorsByMonthsExecutor(
    val commonRepository: CommonRepository,
    val dictionary: Dictionary
) : CommandExecutor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    private class PidorStat(val user: User, val position: Int)

    private val delimiter = "\n========================\n"

    override fun command(): Command {
        return Command.LEADERBOARD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val result = commonRepository
            .getPidorsByChat(update.toChat())
            .filter { it.date.isBefore(startOfMonth()) }
            .groupBy { map(it.date) }
            .mapValues { monthPidors -> calculateStats(monthPidors.value) }
            .toSortedMap()
            .asIterable()
            .reversed()
            .map(formatLeaderBoard())

        val message = "${dictionary.get(Phrase.LEADERBOARD_TITLE)}:\n".bold()
        return {
            it.execute(
                SendMessage(
                    update.toChat().id,
                    message + "\n" + result.joinToString(delimiter)
                ).enableHtml(true)
            )
        }
    }

    private fun formatLeaderBoard(): (Map.Entry<LocalDate, PidorStat>) -> String = {
        "${it.key.month.toRussian().capitalize()}, ${it.key.year}:\n".italic() + "${it.value.user.name.dropLastDelimiter()}, " +
            "${it.value.position} " +
            "${getLeaderboardPhrase(Pluralization.getPlur(it.value.position))} из " +
            "${it.value.position}"
    }

    private fun startOfMonth(): Instant {
        val time = LocalDateTime.now()
        return LocalDateTime.of(time.year, time.month, 1, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    }

    private fun map(instant: Instant): LocalDate {
        val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return LocalDate.of(time.year, time.month, 1)
    }

    private fun calculateStats(pidors: List<Pidor>): PidorStat {
        val pidor = pidors
            .groupBy { it.user }
            .maxBy { it.value.size }
            ?: throw FamilyBot.InternalException("List of pidors should be not empty to calculate stats")
        return PidorStat(pidor.key, pidors.filter { it.user == pidor.key }.count())
    }

    private fun getLeaderboardPhrase(pluralization: Pluralization): String {
        return when (pluralization) {
            Pluralization.ONE -> dictionary.get(Phrase.PLURALIZED_LEADERBOARD_ONE)
            Pluralization.FEW -> dictionary.get(Phrase.PLURALIZED_LEADERBOARD_FEW)
            Pluralization.MANY -> dictionary.get(Phrase.PLURALIZED_LEADERBOARD_MANY)
        }
    }
}

