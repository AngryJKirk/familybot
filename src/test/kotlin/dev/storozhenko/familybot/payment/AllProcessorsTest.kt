package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.suits.FamilybotApplicationTest

class AllProcessorsTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var processors: List<PaymentProcessor>

    @Test
    fun isAllPresentTest() {
        val map = processors.associateBy(PaymentProcessor::itemType)
        ShopItem.values().forEach { item ->
            Assertions.assertTrue(map.contains(item), "Item $item is missing")
        }
    }
}
