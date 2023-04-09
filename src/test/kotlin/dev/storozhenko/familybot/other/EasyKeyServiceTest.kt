package dev.storozhenko.familybot.other

import dev.storozhenko.familybot.infrastructure.randomLong
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.LongKeyType
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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
