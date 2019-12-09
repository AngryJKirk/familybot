package space.yaroslav.familybot.route.services

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Service
class PidorCompetitionService(
    private val repository: CommonRepository,
    private val dictionary: Dictionary
) {

    fun pidorCompetition(update: Update): (suspend (AbsSender) -> Unit)? {
        if (isEndOfMonth()) {
            val thisMonthPidors = getPidorsOfThisMonth(update)
            val competitors = detectPidorCompetition(thisMonthPidors)
            if (competitors != null) {
                return {
                    it.send(update, dictionary.get(Phrase.PIDOR_COMPETITION).bold(), enableHtml = true)
                    val oneMorePidor = competitors.randomNotNull()
                    repository.addPidor(Pidor(oneMorePidor, Instant.now()))
                    delay(1000)
                    val oneMorePidorMessage =
                        dictionary.get(Phrase.COMPETITION_ONE_MORE_PIDOR).bold() + " " + oneMorePidor.getGeneralName()
                    it.send(update, oneMorePidorMessage, enableHtml = true)
                }
            }
        }
        return null
    }

    private fun getPidorsOfThisMonth(update: Update): List<Pidor> {
        val now = LocalDate.now()
        return repository.getPidorsByChat(
            update.message.chat.toChat(),
            startDate = LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
    }

    private fun detectPidorCompetition(pidors: List<Pidor>): Set<User>? {
        val pidorsByUser = pidors.groupBy { it.user }
        val maxCount = pidorsByUser
            .mapValues { it.value.size }
            .maxBy { it.value }
            ?: throw FamilyBot.InternalException("List of pidors for competition should be never null")

        val competitors = pidorsByUser.filterValues { it.size == maxCount.value }.keys
        return if (competitors.size > 1) {
            competitors
        } else {
            null
        }
    }

    private fun isEndOfMonth(): Boolean {
        val time = LocalDateTime.now()
        return time.month.length(time.year % 4 == 0) == time.dayOfMonth
    }
}
