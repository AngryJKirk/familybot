package space.yaroslav.familybot.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import space.yaroslav.familybot.common.utils.from
import space.yaroslav.familybot.infrastructure.TestSender
import space.yaroslav.familybot.infrastructure.createSimpleUpdate
import space.yaroslav.familybot.infrastructure.payload
import space.yaroslav.familybot.infrastructure.randomInt
import space.yaroslav.familybot.infrastructure.randomString
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.services.payment.PaymentService
import space.yaroslav.familybot.services.routers.PaymentRouter
import space.yaroslav.familybot.suits.FamilybotApplicationTest

class PaymentRouterTest : FamilybotApplicationTest() {

    @MockBean
    lateinit var paymentService: PaymentService

    @Autowired
    lateinit var router: PaymentRouter

    private val testSender = TestSender().sender

    @Test
    fun successPreCheckout() {
        whenever(paymentService.processPreCheckoutCheck(any())).thenReturn(null)
        val update = createUpdateWithPreCheckoutQuery()
        runBlocking { router.proceedPreCheckoutQuery(update).invoke(testSender) }
        verify(testSender).execute(eq(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, true)))
    }

    @Test
    fun invalidPreCheckout() {
        whenever(paymentService.processPreCheckoutCheck(any())).thenReturn(Phrase.values().random())
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
        whenever(paymentService.processSuccessfulPayment(any())).thenReturn(Phrase.values().random())
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

    private fun createPayloadJson() = jacksonObjectMapper()
        .writeValueAsString(payload(ShopItem.values().random()))
}