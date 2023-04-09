package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.PaymentRouter
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentService
import dev.storozhenko.familybot.infrastructure.TestSender
import dev.storozhenko.familybot.infrastructure.createSimpleUpdate
import dev.storozhenko.familybot.infrastructure.payload
import dev.storozhenko.familybot.infrastructure.randomInt
import dev.storozhenko.familybot.infrastructure.randomString
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment

class PaymentRouterTest : FamilybotApplicationTest() {

    @MockBean
    lateinit var paymentService: PaymentService

    @Autowired
    lateinit var router: PaymentRouter

    private val testSender = TestSender().sender

    @Test
    fun successPreCheckout() {
        whenever(paymentService.processPreCheckoutCheck(any())).thenReturn(PreCheckOutResponse.Success())
        val update = createUpdateWithPreCheckoutQuery()
        runBlocking { router.proceedPreCheckoutQuery(update).invoke(testSender) }
        verify(testSender).execute(eq(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, true)))
    }

    @Test
    fun invalidPreCheckout() {
        whenever(paymentService.processPreCheckoutCheck(any())).thenReturn(
            PreCheckOutResponse.Error(
                Phrase.values().random()
            )
        )
        val update = createUpdateWithPreCheckoutQuery()
        runBlocking { router.proceedPreCheckoutQuery(update).invoke(testSender) }
        val preCheckoutQueryCaptor = ArgumentCaptor.forClass(AnswerPreCheckoutQuery::class.java)
        verify(testSender, atLeastOnce()).execute(preCheckoutQueryCaptor.capture())
        verify(testSender, atLeastOnce()).execute(any<SendMessage>())
        Assertions.assertEquals(false, preCheckoutQueryCaptor.firstValue.ok)
        Assertions.assertEquals(update.preCheckoutQuery.id, preCheckoutQueryCaptor.firstValue.preCheckoutQueryId)
        Assertions.assertNotNull(preCheckoutQueryCaptor.firstValue.errorMessage)
    }

    @Test
    fun exceptionPreCheckout() {
        whenever(paymentService.processPreCheckoutCheck(any())).thenThrow(RuntimeException())
        val update = createUpdateWithPreCheckoutQuery()
        runBlocking { router.proceedPreCheckoutQuery(update).invoke(testSender) }
        val preCheckoutQueryCaptor = ArgumentCaptor.forClass(AnswerPreCheckoutQuery::class.java)
        verify(testSender, atLeastOnce()).execute(preCheckoutQueryCaptor.capture())
        verify(testSender, atLeastOnce()).execute(any<SendMessage>())
        Assertions.assertEquals(false, preCheckoutQueryCaptor.firstValue.ok)
        Assertions.assertEquals(update.preCheckoutQuery.id, preCheckoutQueryCaptor.firstValue.preCheckoutQueryId)
        Assertions.assertNotNull(preCheckoutQueryCaptor.firstValue.errorMessage)
    }

    @Test
    fun successPayment() {
        whenever(paymentService.processSuccessfulPayment(any())).thenReturn(
            SuccessPaymentResponse(
                Phrase.values().random()
            )
        )
        val update = createUpdateWithSuccessPayment()
        runBlocking { router.proceedSuccessfulPayment(update).invoke(testSender) }
        verify(testSender, times(3)).execute(any<SendMessage>())
    }

    @Test
    fun failedPayment() {
        whenever(paymentService.processSuccessfulPayment(any())).thenThrow(RuntimeException())
        val update = createUpdateWithSuccessPayment()
        runBlocking { router.proceedSuccessfulPayment(update).invoke(testSender) }
        verify(testSender, times(2)).execute(any<SendMessage>())
    }

    private fun createUpdateWithPreCheckoutQuery(): Update {
        return createSimpleUpdate()
            .apply {
                preCheckoutQuery = PreCheckoutQuery(
                    randomString(),
                    this.from(),
                    "RUB",
                    randomInt(),
                    createPayloadJson(),
                    randomString(),
                    null
                )
            }
    }

    private fun createUpdateWithSuccessPayment(): Update {
        return createSimpleUpdate()
            .apply {
                message.successfulPayment = SuccessfulPayment(
                    "RUB",
                    randomInt(),
                    createPayloadJson(),
                    randomString(),
                    null,
                    randomString(),
                    randomString()
                )
            }
    }

    private fun createPayloadJson() =
        payload(ShopItem.values().random()).toJson()
}
