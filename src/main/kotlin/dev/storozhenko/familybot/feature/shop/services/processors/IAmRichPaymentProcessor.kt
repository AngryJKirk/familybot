package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class IAmRichPaymentProcessor : PaymentProcessor {
    override fun itemType() = ShopItem.I_AM_RICH

    override fun preCheckOut(shopPayload: ShopPayload) = PreCheckOutResponse.Success()

    override fun processSuccess(shopPayload: ShopPayload, rawUpdate: Update) = SuccessPaymentResponse(Phrase.I_AM_RICH_DONE)
}
