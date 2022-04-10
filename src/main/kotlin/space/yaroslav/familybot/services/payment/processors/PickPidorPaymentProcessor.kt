package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.EasyKeyValueRepository
import space.yaroslav.familybot.services.settings.PickPidorAbilityCount
import space.yaroslav.familybot.services.settings.UserEasyKey

@Component
class PickPidorPaymentProcessor(
    private val easyKeyValueRepository: EasyKeyValueRepository
) : PaymentProcessor {
    override fun itemType() = ShopItem.PICK_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? = null

    override fun processSuccess(shopPayload: ShopPayload): Pair<Phrase, String?> {
        val key = UserEasyKey(shopPayload.userId)
        val currentValue = easyKeyValueRepository.get(PickPidorAbilityCount, key)
        if (currentValue == null || currentValue <= 0L) {
            easyKeyValueRepository.put(PickPidorAbilityCount, key, 1L)
        } else {
            easyKeyValueRepository.increment(PickPidorAbilityCount, key)
        }
        return Phrase.PICK_PIDOR_DONE to null
    }
}
