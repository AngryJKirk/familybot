package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.services.payment.processors.BetLimitPaymentProcessor
import dev.storozhenko.familybot.services.settings.BetTolerance

class BetLimitPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: BetLimitPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_BET_LIMIT)
        Assertions.assertNotNull(processor.preCheckOut(payload))

        val key = payload.userAndChatKey()
        easyKeyValueService.put(BetTolerance, key, true)
        Assertions.assertTrue(processor.preCheckOut(payload) is PreCheckOutResponse.Success)
        easyKeyValueService.remove(BetTolerance, key)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.DROP_BET_LIMIT)
        val key = payload.userAndChatKey()
        easyKeyValueService.put(BetTolerance, key, true)
        processor.processSuccess(payload)
        Assertions.assertNull(easyKeyValueService.get(BetTolerance, key))
    }
}
