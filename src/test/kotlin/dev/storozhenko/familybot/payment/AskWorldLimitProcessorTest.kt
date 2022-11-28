package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.services.payment.processors.AskWorldLimitPaymentProcessor
import dev.storozhenko.familybot.services.settings.AskWorldChatUsages
import dev.storozhenko.familybot.services.settings.AskWorldUserUsages

class AskWorldLimitProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: AskWorldLimitPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_ASK_WORLD_LIMIT)
        Assertions.assertNotNull(processor.preCheckOut(payload))
        val chatKey = payload.chatKey()
        easyKeyValueService.put(AskWorldChatUsages, chatKey, 1)
        Assertions.assertTrue(processor.preCheckOut(payload) is PreCheckOutResponse.Success)
        easyKeyValueService.remove(AskWorldChatUsages, chatKey)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.DROP_ASK_WORLD_LIMIT)
        val chatKey = payload.chatKey()
        easyKeyValueService.put(AskWorldChatUsages, chatKey, 1)
        val userKey = payload.userKey()
        easyKeyValueService.put(AskWorldUserUsages, userKey, 5)
        processor.processSuccess(payload)
        Assertions.assertNull(easyKeyValueService.get(AskWorldChatUsages, chatKey))
        Assertions.assertNull(easyKeyValueService.get(AskWorldUserUsages, userKey))
    }
}
