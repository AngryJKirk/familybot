package space.yaroslav.familybot.unit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import space.yaroslav.familybot.common.Pluralization
import java.util.stream.Stream

class UtilTest {

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun valuesProvider(): Stream<Arguments> {
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
    @MethodSource("valuesProvider")
    fun plurTest(input: Int, expected: Pluralization) {
        val actual = Pluralization.getPlur(input)
        Assertions.assertEquals(expected, actual)
    }
}
