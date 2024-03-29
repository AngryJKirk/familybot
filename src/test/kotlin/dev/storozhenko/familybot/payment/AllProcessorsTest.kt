package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AllProcessorsTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var processors: List<PaymentProcessor>

    @Test
    fun isAllPresentTest() {
        val map = processors.associateBy(PaymentProcessor::itemType)
        ShopItem.entries.forEach { item ->
            Assertions.assertTrue(map.contains(item), "Item $item is missing")
        }
    }
}
