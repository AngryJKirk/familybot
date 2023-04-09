package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.AutoPidorTimesLeft
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.services.processors.AutoPidorPaymentProcessor
import dev.storozhenko.familybot.infrastructure.payload
import org.junit.jupiter.api.Assertions
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender

class PidorAutoSelectPaymentProcessorTest : PaymentProcessorTest() {

    @Autowired
    lateinit var autoPidorPaymentProcessor: AutoPidorPaymentProcessor

    override fun preCheckOutTest() {
        val payload = payload(ShopItem.AUTO_PIDOR)
        Assertions.assertTrue(autoPidorPaymentProcessor.preCheckOut(payload) is PreCheckOutResponse.Success)
    }

    override fun processSuccessTest() {
        val payload = payload(ShopItem.AUTO_PIDOR)
        val response = autoPidorPaymentProcessor.processSuccess(payload)
        Assertions.assertEquals(Phrase.AUTO_PIDOR_SUCCESS, response.phrase)
        val timesLeft = easyKeyValueService.get(AutoPidorTimesLeft, payload.chatKey(), 0)
        Assertions.assertEquals(30, timesLeft)
        val sender = mock<AbsSender>()
        val customCallCaptor = ArgumentCaptor.forClass(SendMessage::class.java)
        response.customCall(sender)
        verify(sender).execute(customCallCaptor.capture())
        Assertions.assertTrue(customCallCaptor.allValues.size == 1)
        Assertions.assertTrue(customCallCaptor.value.text.endsWith(timesLeft.toString()))
    }
}
