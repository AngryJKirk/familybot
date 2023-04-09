package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.infrastructure.randomLong
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.services.processors.PickPidorPaymentProcessor
import dev.storozhenko.familybot.feature.settings.models.PickPidorAbilityCount
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

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
