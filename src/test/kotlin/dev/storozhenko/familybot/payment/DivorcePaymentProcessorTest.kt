package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.feature.marriage.model.Marriage
import dev.storozhenko.familybot.feature.marriage.repos.MarriagesRepository
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.services.processors.DivorcePaymentProcessor
import dev.storozhenko.familybot.infrastructure.createSimpleUpdate
import dev.storozhenko.familybot.infrastructure.createSimpleUser
import dev.storozhenko.familybot.infrastructure.payload
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

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
