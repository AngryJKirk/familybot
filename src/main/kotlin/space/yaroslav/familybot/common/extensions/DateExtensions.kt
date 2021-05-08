package space.yaroslav.familybot.common.extensions

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

fun Instant.prettyFormat(): String = dateTimeFormatter.format(this)

fun untilNextDay(): Duration =
    Duration.between(Instant.now(), ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant())

fun untilNextMonth(): Duration =
    Duration.between(
        Instant.now(),
        ZonedDateTime.now()
            .truncatedTo(ChronoUnit.DAYS)
            .withDayOfMonth(1)
            .plusMonths(1)
            .toInstant()
    )
