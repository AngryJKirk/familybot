package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.processors.IAmRichPaymentProcessor
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
