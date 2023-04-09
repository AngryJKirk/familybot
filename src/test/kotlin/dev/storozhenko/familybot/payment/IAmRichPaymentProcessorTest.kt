package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.processors.IAmRichPaymentProcessor
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

class IAmRichPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var iAmRichPaymentProcessor: IAmRichPaymentProcessor

    override fun preCheckOutTest() {
        val preCheckOutResponse = iAmRichPaymentProcessor.preCheckOut(payload(ShopItem.I_AM_RICH))
        Assertions.assertTrue(preCheckOutResponse is PreCheckOutResponse.Success)
    }

    override fun processSuccessTest() {
        val result = iAmRichPaymentProcessor.processSuccess(payload(ShopItem.I_AM_RICH))
        Assertions.assertEquals(SuccessPaymentResponse(Phrase.I_AM_RICH_DONE), result)
    }
}
