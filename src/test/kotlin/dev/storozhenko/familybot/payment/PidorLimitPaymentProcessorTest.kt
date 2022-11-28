package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.services.payment.processors.PidorLimitPaymentProcessor
import dev.storozhenko.familybot.services.settings.PidorTolerance

class PidorLimitPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: PidorLimitPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_PIDOR_LIMIT)
        Assertions.assertNotNull(processor.preCheckOut(payload))

        val key = payload.chatKey()
        easyKeyValueService.put(PidorTolerance, key, 1)
        Assertions.assertTrue(processor.preCheckOut(payload) is PreCheckOutResponse.Success)
        easyKeyValueService.remove(PidorTolerance, key)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.DROP_PIDOR_LIMIT)
        val key = payload.chatKey()
        easyKeyValueService.put(PidorTolerance, key, 1)
        processor.processSuccess(payload)
        Assertions.assertNull(easyKeyValueService.get(PidorTolerance, key))
    }
}
