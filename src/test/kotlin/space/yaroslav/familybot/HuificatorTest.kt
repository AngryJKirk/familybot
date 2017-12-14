package space.yaroslav.familybot

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import space.yaroslav.familybot.common.Huificator


@RunWith(JUnit4::class)
class HuificatorTest{


    @Test
    fun test(){
        val huificator = Huificator()
//        println(huificator.huify("привет"))
        println(huificator.huify("абалдеть"))
    }
}