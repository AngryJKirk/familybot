package dev.storozhenko.familybot.feature.shop.services

import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component

@Component
class PaymentService(
    paymentProcessors: List<PaymentProcessor>
) {
    private val log = getLogger()
    private val processors = paymentProcessors.associateBy(PaymentProcessor::itemType)

    fun processPreCheckoutCheck(shopPayload: ShopPayload): PreCheckOutResponse {
        return getProcessor(shopPayload).preCheckOut(shopPayload)
    }

    fun processSuccessfulPayment(shopPayload: ShopPayload): SuccessPaymentResponse {
        return getProcessor(shopPayload).processSuccess(shopPayload)
    }

    private fun getProcessor(shopPayload: ShopPayload): PaymentProcessor {
        val paymentProcessor = (
                processors[shopPayload.shopItem]
                    ?: throw FamilyBot.InternalException("Can't find proper payment processor for ${shopPayload.shopItem}")
                )
        log.info("Payment processor for shopPayload=$shopPayload is ${paymentProcessor::class.java.simpleName}")
        return paymentProcessor
    }
}
