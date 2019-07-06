package space.yaroslav.familybot.route.services

import org.springframework.stereotype.Service
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.randomNotNull
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
    val repository: CommonRepository,
    val dictionary: Dictionary
) {

    fun pidorCompetition(update: Update): ((AbsSender) -> Unit)? {
        val chatId = update.toChat().id
        if (isEndOfMonth()) {
            val now = LocalDate.now()
            val thisMonthPidors = repository.getPidorsByChat(
                update.message.chat.toChat(),
                startDate = LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                    .toInstant(ZoneOffset.UTC)
            )
            val competitors = detectPidorCompetition(thisMonthPidors)
            if (competitors != null) {
                return {
                    it.execute(
                        SendMessage(
                            chatId,
                            dictionary.get(Phrase.PIDOR_COMPETITION).bold()
                        ).enableHtml(true)
                    )
                    val oneMorePidor = competitors.randomNotNull()
                    repository.addPidor(Pidor(oneMorePidor, Instant.now()))
                    Thread.sleep(1000)
                    it.execute(
                        SendMessage(
                            chatId,
                            dictionary.get(Phrase.COMPETITION_ONE_MORE_PIDOR).bold() + " " + oneMorePidor.getGeneralName()
                        ).enableHtml(true)
                    )
                }
            }
        }
        return null
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
