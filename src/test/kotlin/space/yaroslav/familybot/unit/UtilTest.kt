package space.yaroslav.familybot.unit

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import space.yaroslav.familybot.common.Huificator
import space.yaroslav.familybot.common.Pluralization

@RunWith(JUnit4::class)
class UtilTest {

    @Test
    fun test() {
//        println(huificator.huify("привет"))
        println(Huificator.huify("абалдеть"))
    }

    @Test
    fun plurTest() {
        val zero = Pluralization.PluralizationCalc.getPlur(0)
        val few = Pluralization.PluralizationCalc.getPlur(2)
        val many = Pluralization.PluralizationCalc.getPlur(5)
        val elevenIsFew = Pluralization.PluralizationCalc.getPlur(11)
        val one = Pluralization.PluralizationCalc.getPlur(21)
        Assert.assertEquals(zero, Pluralization.MANY)
        Assert.assertEquals(few, Pluralization.FEW)
        Assert.assertEquals(many, Pluralization.MANY)
        Assert.assertEquals(elevenIsFew, Pluralization.MANY)
        Assert.assertEquals(one, Pluralization.ONE)
    }
}
