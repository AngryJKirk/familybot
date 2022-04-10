package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.processors.IAmRichPaymentProcessor

class IAmRichPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var iAmRichPaymentProcessor: IAmRichPaymentProcessor

    override fun preCheckOutTest() {
        Assertions.assertNull(iAmRichPaymentProcessor.preCheckOut(payload(ShopItem.I_AM_RICH)))
    }

    override fun processSuccessTest() {
        val result = iAmRichPaymentProcessor.processSuccess(payload(ShopItem.I_AM_RICH))
        Assertions.assertEquals(Phrase.I_AM_RICH_DONE to null, result)
    }
}
