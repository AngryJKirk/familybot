package dev.storozhenko.familybot.feature.shop.services

import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import org.telegram.telegrambots.meta.api.objects.Update

interface PaymentProcessor {

    fun itemType(): ShopItem

    fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse

    fun processSuccess(shopPayload: ShopPayload, rawUpdate: Update): SuccessPaymentResponse
}
