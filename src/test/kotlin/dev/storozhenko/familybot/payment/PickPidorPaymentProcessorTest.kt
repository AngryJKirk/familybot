package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.infrastructure.randomLong
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.processors.PickPidorPaymentProcessor
import space.yaroslav.familybot.services.settings.PickPidorAbilityCount
import space.yaroslav.familybot.services.settings.UserEasyKey

class PickPidorPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var pickPidorPaymentProcessor: PickPidorPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.PICK_PIDOR)
        Assertions.assertTrue(pickPidorPaymentProcessor.preCheckOut(payload) is PreCheckOutResponse.Success)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.PICK_PIDOR)
        val value = randomLong()
        val key = UserEasyKey(payload.userId)
        easyKeyValueService.put(PickPidorAbilityCount, key, value)
        pickPidorPaymentProcessor.processSuccess(payload)
        Assertions.assertEquals(value + 1, easyKeyValueService.get(PickPidorAbilityCount, key))
    }
}
