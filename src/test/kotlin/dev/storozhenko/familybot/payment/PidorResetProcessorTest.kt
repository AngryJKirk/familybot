package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.infrastructure.createSimpleUpdate
import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Pidor
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.payment.processors.ResetPidorPaymentProcessor
import dev.storozhenko.familybot.services.settings.PidorTolerance
import java.time.Instant

class PidorResetProcessorTest : PaymentProcessorTest() {
    @Autowired
    lateinit var processor: ResetPidorPaymentProcessor

    @Autowired
    lateinit var commonRepository: CommonRepository
    override fun preCheckOutTest() {
        val payload = payload(ShopItem.DROP_PIDOR)
        Assertions.assertNotNull(processor.preCheckOut(payload))

        val user = createSimpleUpdate().toUser().copy(chat = Chat(payload.chatId, null))
        commonRepository.addPidor(Pidor(user, Instant.now()))
        val key = payload.chatKey()
        easyKeyValueService.put(PidorTolerance, key, 1)

        Assertions.assertTrue(processor.preCheckOut(payload) is PreCheckOutResponse.Success)
        easyKeyValueService.remove(PidorTolerance, key)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.DROP_PIDOR)
        val user = createSimpleUpdate().toUser().copy(chat = Chat(payload.chatId, null))
        val key = payload.chatKey()
        commonRepository.addPidor(Pidor(user, Instant.now().minusSeconds(1000)))
        easyKeyValueService.put(PidorTolerance, key, 1)
        processor.processSuccess(payload)
        Assertions.assertNull(easyKeyValueService.get(PidorTolerance, key))
        val pidors = commonRepository.getPidorsByChat(user.chat)
        Assertions.assertEquals(0, pidors.size)
    }
}
