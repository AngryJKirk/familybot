package space.yaroslav.familybot.services.payment

import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.shop.SuccessPaymentResponse

interface PaymentProcessor {

    fun itemType(): ShopItem

    fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse

    fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse
}
