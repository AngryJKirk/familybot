package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.feature.settings.models.AskWorldChatUsages
import dev.storozhenko.familybot.feature.settings.models.AskWorldUserUsages
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.services.processors.AskWorldLimitPaymentProcessor
import dev.storozhenko.familybot.infrastructure.createUpdateForPayment
import dev.storozhenko.familybot.infrastructure.payload
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

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
        processor.processSuccess(payload, createUpdateForPayment(payload))
        Assertions.assertNull(easyKeyValueService.get(AskWorldChatUsages, chatKey))
        Assertions.assertNull(easyKeyValueService.get(AskWorldUserUsages, userKey))
    }
}
