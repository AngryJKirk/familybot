package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.payment.processors.ResetPidorPaymentProcessor
import space.yaroslav.familybot.services.settings.PidorTolerance
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

        Assertions.assertNull(processor.preCheckOut(payload))
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