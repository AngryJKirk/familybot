package space.yaroslav.familybot.common.utils

import org.telegram.telegrambots.meta.api.objects.Chat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

fun Instant.isToday(): Boolean {
    return LocalDate.now().atTime(0, 0).isBefore(LocalDateTime.ofInstant(this, ZoneId.of("UTC")))
}

val monthMap = mapOf(
    Month.JANUARY to "январь",
    Month.FEBRUARY to "февраль",
    Month.MARCH to "март",
    Month.APRIL to "апрель",
    Month.MAY to "май",
    Month.JUNE to "июнь",
    Month.JULY to "июль",
    Month.AUGUST to "август",
    Month.SEPTEMBER to "сентябрь",
    Month.OCTOBER to "октябрь",
    Month.NOVEMBER to "ноябрь",
    Month.DECEMBER to "декабрь"
)

fun Month.toRussian(): String {
    return monthMap.getValue(this)
}

fun Chat.isGroup(): Boolean {
    return this.isSuperGroupChat || this.isGroupChat
}

fun Boolean.toEmoji(): String {
    return when (this) {
        true -> "✅"
        false -> "❌"
    }
}






