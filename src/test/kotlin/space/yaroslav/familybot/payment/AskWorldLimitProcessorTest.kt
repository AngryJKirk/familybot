package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.ShopItem
import space.yaroslav.familybot.models.chatKey
import space.yaroslav.familybot.models.userKey
import space.yaroslav.familybot.services.payment.processors.AskWorldLimitPaymentProcessor
import space.yaroslav.familybot.services.settings.AskWorldChatUsages
import space.yaroslav.familybot.services.settings.AskWorldUserUsages

class AskWorldLimitProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: AskWorldLimitPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_ASK_WORLD_LIMIT)
        Assertions.assertNotNull(processor.preCheckOut(payload))
        val chatKey = payload.chatKey()
        easyKeyValueService.put(AskWorldChatUsages, chatKey, 1)
        Assertions.assertNull(processor.preCheckOut(payload))
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