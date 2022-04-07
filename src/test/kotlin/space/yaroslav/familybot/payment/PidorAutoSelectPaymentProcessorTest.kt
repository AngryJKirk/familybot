package space.yaroslav.familybot.payment

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.processors.AutoPidorPaymentProcessor
import space.yaroslav.familybot.services.settings.AutoPidorTimesLeft

class PidorAutoSelectPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var autoPidorPaymentProcessor: AutoPidorPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.AUTO_PIDOR)
        Assertions.assertNull(autoPidorPaymentProcessor.preCheckOut(payload))
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.AUTO_PIDOR)
        val (phrase, comment) = autoPidorPaymentProcessor.processSuccess(payload)
        Assertions.assertEquals(Phrase.AUTO_PIDOR_SUCCESS, phrase)
        val timesLeft = easyKeyValueService.get(AutoPidorTimesLeft, payload.chatKey(), 0)
        Assertions.assertEquals(30, timesLeft)
        Assertions.assertNotNull(comment)
        Assertions.assertTrue(comment?.endsWith(timesLeft.toString()) ?: false)
    }
}