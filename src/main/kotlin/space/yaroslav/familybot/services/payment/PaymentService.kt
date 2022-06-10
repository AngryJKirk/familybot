package space.yaroslav.familybot.services.payment

import org.springframework.stereotype.Component
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.shop.SuccessPaymentResponse
import space.yaroslav.familybot.telegram.FamilyBot

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
