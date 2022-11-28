package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.createSimpleUser
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.Marriage
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.repos.MarriagesRepository
import space.yaroslav.familybot.services.payment.processors.DivorcePaymentProcessor

class DivorcePaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var processor: DivorcePaymentProcessor

    @Autowired
    lateinit var marriagesRepository: MarriagesRepository

    override fun preCheckOutTest() {
        Assertions.assertNotNull(processor.preCheckOut(payload(ShopItem.DIVORCE)))

        val (payload, marriage) = createMarriageAndPayload()
        Assertions.assertTrue(processor.preCheckOut(payload) is PreCheckOutResponse.Success)
        marriagesRepository.removeMarriage(marriage.chatId, marriage.firstUser.id)
    }

    override fun processSuccessTest() {
        val (payload, marriage) = createMarriageAndPayload()
        processor.processSuccess(payload)
        Assertions.assertNull(marriagesRepository.getMarriage(marriage.chatId, marriage.firstUser.id))
    }

    private fun createMarriageAndPayload(): Pair<ShopPayload, Marriage> {
        val update = createSimpleUpdate()
        val chat = update.toChat()
        val user = update.toUser()
        val userToDivorceWith = createSimpleUser().toUser(chat = chat)
        val payload = ShopPayload(chatId = chat.id, user.id, ShopItem.DIVORCE)
        val marriage = Marriage(payload.chatId, user, userToDivorceWith)
        marriagesRepository.addMarriage(marriage)
        return payload to marriage
    }
}
