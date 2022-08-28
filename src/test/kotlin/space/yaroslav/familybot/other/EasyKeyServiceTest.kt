package space.yaroslav.familybot.other

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.randomLong
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.LongKeyType
import space.yaroslav.familybot.suits.FamilybotApplicationTest

class EasyKeyServiceTest : FamilybotApplicationTest() {

    @Autowired
    private lateinit var easyKeyValueService: EasyKeyValueService

    @Test
    fun getAllByPartialKey() {
        val expectedData = (1..10)
            .associate { ChatEasyKey(randomLong()) to randomLong() }
        expectedData.forEach { (key, value) -> easyKeyValueService.put(TestKey, key, value) }
        val actualData = easyKeyValueService.getAllByPartKey(TestKey)
        Assertions.assertEquals(expectedData, actualData)
    }

    private object TestKey : LongKeyType<ChatEasyKey>
}
