package space.yaroslav.familybot.services.pidor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.bold
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.telegram.FamilyBot
import java.lang.Integer.max

@Component
class PidorStrikesService(
    private val pidorStrikeStorage: PidorStrikeStorage
) {
    fun calculateStrike(executorContext: ExecutorContext, pidor: User): suspend (AbsSender) -> Unit {
        val stats = pidorStrikeStorage.get(executorContext.chat)
        val newStats = calculateStrike(stats, pidor)

        pidorStrikeStorage.save(executorContext, newStats)

        val newPidorStrike = newStats.stats[pidor.id]
            ?: throw FamilyBot.InternalException("Some huge internal logic problem, please investigate")
        return if (newPidorStrike.currentStrike >= 2 && newStats.stats.size > 1) {
            congratulate(executorContext, newPidorStrike)
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
        executorContext: ExecutorContext,
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
            sender.send(
                executorContext,
                executorContext.phrase(phrase).bold(),
                shouldTypeBeforeSend = true,
                enableHtml = true
            )
        }
    }
}
