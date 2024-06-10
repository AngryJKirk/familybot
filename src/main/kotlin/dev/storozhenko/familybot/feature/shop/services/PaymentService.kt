package dev.storozhenko.familybot.feature.shop.services

import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PaymentService(
    paymentProcessors: List<PaymentProcessor>,
) {
    private val log = KotlinLogging.logger { }
    private val processors = paymentProcessors.associateBy(PaymentProcessor::itemType)

    fun processPreCheckoutCheck(shopPayload: ShopPayload) = getProcessor(shopPayload).preCheckOut(shopPayload)


    fun processSuccessfulPayment(shopPayload: ShopPayload, chargeId: String) = getProcessor(shopPayload).processSuccess(shopPayload)

    private fun getProcessor(shopPayload: ShopPayload): PaymentProcessor {
        val paymentProcessor = processors[shopPayload.shopItem]
                    ?: throw FamilyBot.InternalException("Can't find proper payment processor for ${shopPayload.shopItem}")

        log.info { "Payment processor for shopPayload=$shopPayload is ${paymentProcessor::class.java.simpleName}" }
        return paymentProcessor
    }
}
