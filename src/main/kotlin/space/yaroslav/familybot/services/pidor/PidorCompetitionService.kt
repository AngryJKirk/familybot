package space.yaroslav.familybot.services.pidor

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.startOfCurrentMonth
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant
import java.time.LocalDate

@Service
class PidorCompetitionService(
    private val repository: CommonRepository
) {

    fun pidorCompetition(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        if (isEndOfMonth()) {
            val thisMonthPidors = getPidorsOfThisMonth(executorContext.chat)
            if (thisMonthPidors.size < 2) {
                return { }
            }
            val competitors = detectPidorCompetition(thisMonthPidors)
            if (competitors != null) {
                return {
                    it.send(
                        executorContext,
                        executorContext.phrase(Phrase.PIDOR_COMPETITION).bold() + "\n" + formatListOfCompetitors(
                            competitors
                        ),
                        enableHtml = true
                    )
                    val oneMorePidor = competitors.random()
                    repository.addPidor(Pidor(oneMorePidor, Instant.now()))
                    delay(1000)
                    val oneMorePidorMessage =
                        executorContext.phrase(Phrase.COMPETITION_ONE_MORE_PIDOR)
                            .bold() + " " + oneMorePidor.getGeneralName()
                    it.send(executorContext, oneMorePidorMessage, enableHtml = true)
                }
            }
        }
        return { }
    }

    private fun getPidorsOfThisMonth(chat: Chat): List<Pidor> {
        return repository.getPidorsByChat(
            chat,
            startDate = startOfCurrentMonth()
        )
    }

    private fun detectPidorCompetition(pidors: List<Pidor>): Set<User>? {
        val pidorsByUser = pidors.groupBy(Pidor::user)
        val maxCount = pidorsByUser
            .mapValues { it.value.size }
            .maxByOrNull(Map.Entry<User, Int>::value)
            ?: throw FamilyBot.InternalException("List of pidors for competition should be never null")

        return pidorsByUser
            .filterValues { it.size == maxCount.value }
            .keys
            .takeIf { it.size > 1 }
    }

    private fun isEndOfMonth(): Boolean {
        val time = LocalDate.now()
        return time.lengthOfMonth() == time.dayOfMonth
    }

    private fun formatListOfCompetitors(users: Set<User>): String {
        return users
            .joinToString(separator = " vs. ".bold()) { user -> user.getGeneralName() }
    }
}
