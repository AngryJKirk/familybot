package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.feature.settings.models.PickPidorAbilityCount
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import org.springframework.stereotype.Component

@Component
class PickPidorPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    override fun itemType() = ShopItem.PICK_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse = PreCheckOutResponse.Success()

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        val key = UserEasyKey(shopPayload.userId)
        val currentValue = easyKeyValueService.get(PickPidorAbilityCount, key)
        if (currentValue == null || currentValue <= 0L) {
            easyKeyValueService.put(PickPidorAbilityCount, key, 1L)
        } else {
            easyKeyValueService.increment(PickPidorAbilityCount, key)
        }
        return SuccessPaymentResponse(Phrase.PICK_PIDOR_DONE)
    }
}
