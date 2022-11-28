package dev.storozhenko.familybot.services.payment

import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse

interface PaymentProcessor {

    fun itemType(): ShopItem

    fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse

    fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse
}
