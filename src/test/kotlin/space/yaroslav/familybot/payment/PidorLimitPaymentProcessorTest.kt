package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.processors.PidorLimitPaymentProcessor
import space.yaroslav.familybot.services.settings.PidorTolerance

class PidorLimitPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: PidorLimitPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_PIDOR_LIMIT)
        Assertions.assertNotNull(processor.preCheckOut(payload))

        val key = payload.chatKey()
        easyKeyValueService.put(PidorTolerance, key, 1)
        Assertions.assertNull(processor.preCheckOut(payload))
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