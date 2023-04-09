package dev.storozhenko.familybot.feature.pidor.services

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.pidor.repos.PidorStrikeStat
import dev.storozhenko.familybot.feature.pidor.repos.PidorStrikeStorage
import dev.storozhenko.familybot.feature.pidor.repos.PidorStrikes
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import java.lang.Integer.max

@Component
class PidorStrikesService(
    private val pidorStrikeStorage: PidorStrikeStorage,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) {
    fun calculateStrike(chat: Chat, chatEasyKey: ChatEasyKey, pidor: User): suspend (AbsSender) -> Unit {
        val stats = pidorStrikeStorage.get(chatEasyKey)
        val newStats = calculateStrike(stats, pidor)

        pidorStrikeStorage.save(chatEasyKey, newStats)

        val newPidorStrike = newStats.stats[pidor.id]
            ?: throw FamilyBot.InternalException("Some huge internal logic problem, please investigate")
        return if (newPidorStrike.currentStrike >= 2 && newStats.stats.size > 1) {
            congratulate(chat, chatEasyKey, newPidorStrike)
        } else {
            { }
        }
    }

    private fun calculateStrike(
        stats: PidorStrikes,
        pidor: User
    ): PidorStrikes {
        val currentValue = stats.stats[pidor.id] ?: PidorStrikeStat(0, 0)
        val nextStrikeValue = currentValue.currentStrike + 1

        return PidorStrikes(
            stats
                .stats
                .filter { (key) -> key != pidor.id }
                .map { (key, value) -> key to PidorStrikeStat(0, value.maxStrike) }
                .plus(
                    pidor.id to PidorStrikeStat(
                        nextStrikeValue,
                        max(nextStrikeValue, currentValue.maxStrike)
                    )
                )
                .toMap()
        )
    }

    private fun congratulate(
        chat: Chat,
        chatEasyKey: ChatEasyKey,
        strike: PidorStrikeStat
    ): suspend (AbsSender) -> Unit {
        val phrase = when (strike.currentStrike) {
            2 -> Phrase.PIDOR_STRIKE_2
            3 -> Phrase.PIDOR_STRIKE_3
            4 -> Phrase.PIDOR_STRIKE_4
            5 -> Phrase.PIDOR_STRIKE_5
            6 -> Phrase.PIDOR_STRIKE_6
            7 -> Phrase.PIDOR_STRIKE_7
            8 -> Phrase.PIDOR_STRIKE_8
            9 -> Phrase.PIDOR_STRIKE_9
            10 -> Phrase.PIDOR_STRIKE_10
            else -> Phrase.PIDOR_STRIKE_ELSE
        }
        return { sender ->
            sender.sendContextFree(
                chat.idString,
                dictionary.get(phrase, chatEasyKey).bold(),
                botConfig,
                shouldTypeBeforeSend = true,
                enableHtml = true
            )
        }
    }
}
