package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.processors.IAmRichPaymentProcessor
import dev.storozhenko.familybot.infrastructure.createUpdateForPayment
import dev.storozhenko.familybot.infrastructure.payload
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired

class IAmRichPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var iAmRichPaymentProcessor: IAmRichPaymentProcessor

    override fun preCheckOutTest() {
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.I_AM_RICH)
        val result = iAmRichPaymentProcessor.processSuccess(payload, createUpdateForPayment(payload))
        Assertions.assertEquals(SuccessPaymentResponse(Phrase.I_AM_RICH_DONE), result)
    }
}
