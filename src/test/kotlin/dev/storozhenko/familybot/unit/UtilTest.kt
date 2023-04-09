package dev.storozhenko.familybot.unit

import dev.storozhenko.familybot.common.extensions.formatTopList
import dev.storozhenko.familybot.common.extensions.startOfCurrentMonth
import dev.storozhenko.familybot.common.extensions.startOfDay
import dev.storozhenko.familybot.common.extensions.startOfTheYear
import dev.storozhenko.familybot.common.extensions.toRussian
import dev.storozhenko.familybot.core.models.dictionary.Pluralization
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.infrastructure.randomLong
import dev.storozhenko.familybot.infrastructure.randomString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.stream.Stream

class UtilTest {

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun plurTestValues(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0, Pluralization.MANY),
                Arguments.of(2, Pluralization.FEW),
                Arguments.of(5, Pluralization.MANY),
                Arguments.of(11, Pluralization.MANY),
                Arguments.of(21, Pluralization.ONE)
            )
        }

        @JvmStatic
        fun monthsTestValues(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Month.JANUARY, "январь"),
                Arguments.of(Month.FEBRUARY, "февраль"),
                Arguments.of(Month.MARCH, "март"),
                Arguments.of(Month.APRIL, "апрель"),
                Arguments.of(Month.MAY, "май"),
                Arguments.of(Month.JUNE, "июнь"),
                Arguments.of(Month.JULY, "июль"),
                Arguments.of(Month.AUGUST, "август"),
                Arguments.of(Month.SEPTEMBER, "сентябрь"),
                Arguments.of(Month.OCTOBER, "октябрь"),
                Arguments.of(Month.NOVEMBER, "ноябрь"),
                Arguments.of(Month.DECEMBER, "декабрь")
            )
        }
    }

    @ParameterizedTest
    @MethodSource("plurTestValues")
    fun plurTest(input: Int, expected: Pluralization) {
        val actual = Pluralization.getPlur(input)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun startOfDayTest() {
        val startOfDay = ZonedDateTime.ofInstant(startOfDay(), ZoneOffset.UTC)
        val currentDate = ZonedDateTime.now(ZoneId.of("UTC"))

        Assertions.assertEquals(0, startOfDay.hour)
        Assertions.assertEquals(0, startOfDay.minute)
        Assertions.assertEquals(currentDate.month, startOfDay.month)
        Assertions.assertEquals(currentDate.dayOfMonth, startOfDay.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfDay.year)
    }

    @Test
    fun startOfMonthTest() {
        val startOfMonth = ZonedDateTime.ofInstant(startOfCurrentMonth(), ZoneOffset.UTC)
        val currentDate = ZonedDateTime.now(ZoneId.of("UTC"))

        Assertions.assertEquals(0, startOfMonth.hour)
        Assertions.assertEquals(0, startOfMonth.minute)
        Assertions.assertEquals(currentDate.month, startOfMonth.month)
        Assertions.assertEquals(1, startOfMonth.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfMonth.year)
    }

    @Test
    fun startOfYearTest() {
        val startOfYear = ZonedDateTime.ofInstant(startOfTheYear(), ZoneOffset.UTC)
        val currentDate = ZonedDateTime.now(ZoneId.of("UTC"))

        Assertions.assertEquals(0, startOfYear.hour)
        Assertions.assertEquals(0, startOfYear.minute)
        Assertions.assertEquals(Month.JANUARY, startOfYear.month)
        Assertions.assertEquals(1, startOfYear.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfYear.year)
    }

    @ParameterizedTest
    @MethodSource("monthsTestValues")
    fun monthTest(month: Month, result: String) {
        Assertions.assertEquals(result, month.toRussian())
    }

    @Test
    fun formatTopListTest() {
        val chat = Chat(randomLong(), randomString())
        val commonUserName = randomString()
        val user1 = User(1, chat, commonUserName, randomString())
        val user2 = User(2, chat, commonUserName, randomString())
        val user3 = User(3, chat, randomString(), randomString())
        val topList = listOf(
            user1,
            user1,
            user1,
            user2,
            user2,
            user2,
            user3,
            user3,
            user3
        ).formatTopList()
        Assertions.assertEquals(3, topList.size)
    }
}
