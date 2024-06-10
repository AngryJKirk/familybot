package dev.storozhenko.familybot.feature.pidor.services

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.pidor.models.PidorStrikeStat
import dev.storozhenko.familybot.feature.pidor.models.PidorStrikes
import dev.storozhenko.familybot.feature.settings.models.PidorStrikeStats
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.lang.Integer.max

@Component
class PidorStrikesService(
    private val easyKeyValueService: EasyKeyValueService,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
) {
    fun calculateStrike(chat: Chat, chatEasyKey: ChatEasyKey, pidor: User): suspend (TelegramClient) -> Unit {
        val stats = easyKeyValueService.get(PidorStrikeStats, chatEasyKey, PidorStrikes())
        val newStats = calculateStrike(stats, pidor)

        easyKeyValueService.put(PidorStrikeStats, chatEasyKey, newStats)

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
        pidor: User,
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
                        max(nextStrikeValue, currentValue.maxStrike),
                    ),
                )
                .toMap(),
        )
    }

    private fun congratulate(
        chat: Chat,
        chatEasyKey: ChatEasyKey,
        strike: PidorStrikeStat,
    ): suspend (TelegramClient) -> Unit {
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
        return { client ->
            client.sendContextFree(
                chat.idString,
                dictionary.get(phrase, chatEasyKey).bold(),
                botConfig,
                shouldTypeBeforeSend = true,
                enableHtml = true,
            )
        }
    }
}
