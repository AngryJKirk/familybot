package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.suits.FamilybotApplicationTest

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
