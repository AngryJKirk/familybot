package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.marriage.repos.MarriagesRepository
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import org.springframework.stereotype.Component

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
