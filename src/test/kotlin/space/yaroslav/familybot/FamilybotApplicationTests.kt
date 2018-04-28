package space.yaroslav.familybot

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.telegram.telegrambots.ApiContextInitializer

@RunWith(SpringRunner::class)
@SpringBootTest
class FamilybotApplicationTests {

    @Before
    fun setUp() = ApiContextInitializer.init()

    @Test
    fun contextLoads() {
    }
}
