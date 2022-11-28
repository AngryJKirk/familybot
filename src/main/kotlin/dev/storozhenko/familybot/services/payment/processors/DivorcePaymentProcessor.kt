package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.repos.MarriagesRepository
import dev.storozhenko.familybot.services.payment.PaymentProcessor

@Component
class DivorcePaymentProcessor(
    private val marriagesRepository: MarriagesRepository
) : PaymentProcessor {
    override fun itemType() = ShopItem.DIVORCE

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val marriage = marriagesRepository.getMarriage(shopPayload.chatId, shopPayload.userId)
        return if (marriage != null) {
            PreCheckOutResponse.Success()
        } else {
            PreCheckOutResponse.Error(Phrase.DIVORCE_PRE_CHECKOUT_MARRIAGE_NOT_FOUND)
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        marriagesRepository.removeMarriage(shopPayload.chatId, shopPayload.userId)
        return SuccessPaymentResponse(Phrase.MARRY_DIVORCE)
    }
}
