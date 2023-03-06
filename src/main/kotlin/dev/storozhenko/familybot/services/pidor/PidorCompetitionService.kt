package dev.storozhenko.familybot.services.pidor

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.common.extensions.startOfCurrentMonth
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Pidor
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.talking.Dictionary
import dev.storozhenko.familybot.telegram.BotConfig
import dev.storozhenko.familybot.telegram.FamilyBot
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@Service
class PidorCompetitionService(
    private val repository: CommonRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) {

    fun pidorCompetition(chat: Chat, chatEasyKey: ChatEasyKey): suspend (AbsSender) -> Unit {
        if (isEndOfMonth()) {
            val thisMonthPidors = getPidorsOfThisMonth(chat)
            if (thisMonthPidors.size < 2) {
                return { }
            }
            val competitors = detectPidorCompetition(thisMonthPidors)
            if (competitors != null) {
                return {
                    it.sendContextFree(
                        chat.idString,
                        dictionary.get(Phrase.PIDOR_COMPETITION, chatEasyKey).bold() + "\n" + formatListOfCompetitors(
                            competitors
                        ),
                        botConfig,
                        enableHtml = true
                    )
                    val oneMorePidor = competitors.random()
                    repository.addPidor(Pidor(oneMorePidor, Instant.now()))
                    delay(1.seconds)
                    val oneMorePidorMessage =
                        dictionary.get(Phrase.COMPETITION_ONE_MORE_PIDOR, chatEasyKey)
                            .bold() + " " + oneMorePidor.getGeneralName()
                    it.sendContextFree(chat.idString, oneMorePidorMessage, botConfig, enableHtml = true)
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
