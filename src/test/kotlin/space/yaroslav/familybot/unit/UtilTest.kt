package space.yaroslav.familybot.unit

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import space.yaroslav.familybot.common.Pluralization

@RunWith(JUnit4::class)
class UtilTest {

    @Test
    fun plurTest() {
        val zero = Pluralization.getPlur(0)
        val few = Pluralization.getPlur(2)
        val many = Pluralization.getPlur(5)
        val elevenIsFew = Pluralization.getPlur(11)
        val one = Pluralization.getPlur(21)
        Assert.assertEquals(zero, Pluralization.MANY)
        Assert.assertEquals(few, Pluralization.FEW)
        Assert.assertEquals(many, Pluralization.MANY)
        Assert.assertEquals(elevenIsFew, Pluralization.MANY)
        Assert.assertEquals(one, Pluralization.ONE)
    }
}
