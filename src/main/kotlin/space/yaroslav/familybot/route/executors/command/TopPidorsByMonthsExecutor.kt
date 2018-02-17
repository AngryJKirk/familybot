package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.*
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

@Component
class TopPidorsByMonthsExecutor(val commonRepository: CommonRepository,
                                val pidorDictionaryRepository: PidorDictionaryRepository) : CommandExecutor(), Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }
    private data class PidorDate(val month: Month, val year: Int) : Comparable<PidorDate> {
        override fun compareTo(other: PidorDate): Int {
            if (year > other.year) {
                return -1
            }
            if (month.value > other.month.value) {
                return -1
            } else if (month.value < other.month.value) {
                return 1
            }
            return 0
        }
    }

    private class PidorStat(val user: User, val position: Int)

    private val delimiter = "\n========================\n"

    override fun command(): Command {
        return Command.LEADERBOARD
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val pidors = commonRepository
                .getPidorsByChat(update.toChat())
                .filter { it.date.isBefore(startOfMonth()) }
        val result = pidors
                .groupBy { map(it.date) }
                .mapValues { monthPidors -> calculateStats(monthPidors.value) }
                .toSortedMap()
                .asIterable()
                .reversed()
                .map { "${it.key.month.toRussian().capitalize()}, ${it.key.year}:\n".italic() + "${it.value.user.name.dropLastDelimiter()}, "+
                        "${it.value.position} " +
                        "${pidorDictionaryRepository.getLeaderBoardPhrase(Pluralization.PluralizationCalc.getPlur(it.value.position)).random()} из " +
                        "${it.value.position}"}

        val message = "Ими гордится школа:\n".bold()
        return {
            it.execute(SendMessage(update.toChat().id,
                    message + "\n" + result.joinToString(delimiter)).enableHtml(true))
        }
    }

    private fun startOfMonth(): Instant {
        val time = LocalDateTime.now()
        return LocalDateTime.of(time.year, time.month, 1, 0, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
    }

    private fun map(instant: Instant): PidorDate {
        val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return PidorDate(time.month, time.year)
    }

    private fun calculateStats(pidors: List<Pidor>): PidorStat{
        val pidor = pidors
                .groupBy { it.user }
                .maxBy { it.value.size }!!
        return PidorStat(pidor.key, pidors.filter { it.user == pidor.key }.count())
    }



    }

