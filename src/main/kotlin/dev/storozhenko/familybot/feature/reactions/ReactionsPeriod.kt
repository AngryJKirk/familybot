package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class ReactionsPeriod(val periodName: String, val period: Duration) {
    DAY("день", 24.hours),
    WEEK("неделя", 7.days),
    MONTH("месяц", 30.days);


    companion object {
        private const val AI_PREFIX = "AI "
        fun parse(period: String): Pair<ReactionsPeriod, Boolean> {
            val isAi = period.contains(AI_PREFIX)
            val normalizedPeriod = period.replace(AI_PREFIX, "")
            val reactionsPeriod = entries.find { it.periodName == normalizedPeriod }
                ?: throw FamilyBot.InternalException("Unknown reactions period $normalizedPeriod")
            return reactionsPeriod to isAi
        }

        fun toKeyBoard(): InlineKeyboardMarkup {
            return InlineKeyboardMarkup(
                listOf(
                    entries.map {
                        InlineKeyboardButton
                            .builder()
                            .text(it.periodName)
                            .callbackData(it.periodName)
                            .build()
                    },
                    entries.map {
                        InlineKeyboardButton
                            .builder()
                            .text(AI_PREFIX + it.periodName)
                            .callbackData(AI_PREFIX + it.periodName)
                            .build()
                    })
            )

        }
    }
}