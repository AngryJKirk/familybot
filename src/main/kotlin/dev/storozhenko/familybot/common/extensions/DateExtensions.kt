package dev.storozhenko.familybot.common.extensions

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

object DateConstants {
    const val VITYA_MORTGAGE_DATE = 1678706466L
    val theBirthDayOfFamilyBot: Instant = LocalDateTime.of(
        2017,
        Month.DECEMBER,
        18,
        17,
        36
    ).toInstant(ZoneOffset.UTC)
}

fun Instant.isToday(): Boolean {
    val startOfDay = Instant.now().startOfDay()
    return startOfDay.isBefore(this) || startOfDay == this
}

fun Instant.startOfDay(): Instant {
    return this.truncatedTo(ChronoUnit.DAYS)
}

fun Duration.toHourMinuteString(): String {
    val hour = toHours().toInt()
    val minute = (toMinutes() % 60).toInt()
    val hourPluralized = pluralize(
        hour,
        PluralizedWordsProvider(
            one = { "час" },
            few = { "часа" },
            many = { "часов" }
        )
    )
    val minutePluralized = pluralize(
        minute,
        PluralizedWordsProvider(
            one = { "минута" },
            few = { "минуты" },
            many = { "минут" }
        )
    )

    return "$hour $hourPluralized и $minute $minutePluralized"
}

private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

private val dateTimeFormatterDateOnly =
    DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

fun Instant.prettyFormat(dateOnly: Boolean = false): String {
    return if (dateOnly) {
        dateTimeFormatterDateOnly.format(this)
    } else {
        dateTimeFormatter.format(this)
    }
}

fun untilNextDay(): kotlin.time.Duration {
    val currentTime = Instant.now()
    val startOfNextDay = LocalDateTime
        .now()
        .truncatedTo(ChronoUnit.DAYS)
        .plusDays(1)
        .toInstant(ZoneOffset.UTC)

    return Duration.between(currentTime, startOfNextDay).toKotlinDuration()
}

fun untilNextMonth(): kotlin.time.Duration {
    val currentTime = Instant.now()
    val startOfNextMonth = LocalDateTime.now()
        .truncatedTo(ChronoUnit.DAYS)
        .withDayOfMonth(1)
        .plusMonths(1)
        .toInstant(ZoneOffset.UTC)

    return Duration.between(currentTime, startOfNextMonth).toKotlinDuration()
}

fun startOfCurrentMonth(): Instant =
    LocalDateTime
        .now()
        .truncatedTo(ChronoUnit.DAYS)
        .withDayOfMonth(1)
        .toInstant(ZoneOffset.UTC)

fun startOfDay(): Instant = Instant.now().startOfDay()

fun startOfTheYear(): Instant =
    LocalDateTime
        .now()
        .truncatedTo(ChronoUnit.DAYS)
        .withDayOfYear(1)
        .toInstant(ZoneOffset.UTC)
