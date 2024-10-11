package dev.storozhenko.familybot.feature.pidor.services

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.common.extensions.startOfCurrentMonth
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@Component
class PidorCompetitionService(
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
    private val pidorRepository: PidorRepository,
) {

    fun pidorCompetition(chat: Chat, chatEasyKey: ChatEasyKey): suspend (TelegramClient) -> Unit {
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
                            competitors,
                        ),
                        botConfig,
                        enableHtml = true,
                    )
                    val oneMorePidor = competitors.random()
                    pidorRepository.addPidor(Pidor(oneMorePidor, Instant.now()))
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
        return pidorRepository.getPidorsByChat(
            chat,
            startDate = startOfCurrentMonth(),
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
