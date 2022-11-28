package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.PickPidorAbilityCount
import dev.storozhenko.familybot.services.settings.UserEasyKey

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
