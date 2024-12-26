package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.reactions.ReactionsPeriod.entries
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class ReactionsPeriod(val periodName: String, val period: Duration) {
    DAY("день", 24.hours),
    WEEK("неделя", 7.days),
    MONTH("месяц", 30.days);

    companion object {
        const val AI_PREFIX = "AI "
        fun parse(period: String): Pair<ReactionsPeriod, Boolean> {
            val isAi = period.contains(AI_PREFIX)
            val normalizedPeriod = period.replace(AI_PREFIX, "")
            val reactionsPeriod = entries.find { it.periodName == normalizedPeriod }
                ?: throw FamilyBot.InternalException("Unknown reactions period $normalizedPeriod")
            return reactionsPeriod to isAi
        }
    }
}