package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.shop.SuccessPaymentResponse
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PickPidorAbilityCount
import space.yaroslav.familybot.services.settings.UserEasyKey

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
