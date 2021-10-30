package space.yaroslav.familybot.unit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import space.yaroslav.familybot.common.extensions.startOfCurrentMonth
import space.yaroslav.familybot.common.extensions.startOfDay
import space.yaroslav.familybot.common.extensions.startOfTheYear
import space.yaroslav.familybot.models.dictionary.Pluralization
import java.time.Month
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
        val currentDate = ZonedDateTime.now()

        Assertions.assertEquals(0, startOfDay.hour)
        Assertions.assertEquals(0, startOfDay.minute)
        Assertions.assertEquals(currentDate.month, startOfDay.month)
        Assertions.assertEquals(currentDate.dayOfMonth, startOfDay.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfDay.year)
    }

    @Test
    fun startOfMonthTest() {
        val startOfMonth = ZonedDateTime.ofInstant(startOfCurrentMonth(), ZoneOffset.UTC)
        val currentDate = ZonedDateTime.now()

        Assertions.assertEquals(0, startOfMonth.hour)
        Assertions.assertEquals(0, startOfMonth.minute)
        Assertions.assertEquals(currentDate.month, startOfMonth.month)
        Assertions.assertEquals(1, startOfMonth.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfMonth.year)
    }

    @Test
    fun startOfYearTest() {
        val startOfYear = ZonedDateTime.ofInstant(startOfTheYear(), ZoneOffset.UTC)
        val currentDate = ZonedDateTime.now()

        Assertions.assertEquals(0, startOfYear.hour)
        Assertions.assertEquals(0, startOfYear.minute)
        Assertions.assertEquals(Month.JANUARY, startOfYear.month)
        Assertions.assertEquals(1, startOfYear.dayOfMonth)
        Assertions.assertEquals(currentDate.year, startOfYear.year)
    }
}
