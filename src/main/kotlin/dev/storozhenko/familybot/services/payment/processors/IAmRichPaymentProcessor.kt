package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor

@Component
class IAmRichPaymentProcessor : PaymentProcessor {
    override fun itemType() = ShopItem.I_AM_RICH

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse = PreCheckOutResponse.Success()

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        return SuccessPaymentResponse(Phrase.I_AM_RICH_DONE)
    }
}
