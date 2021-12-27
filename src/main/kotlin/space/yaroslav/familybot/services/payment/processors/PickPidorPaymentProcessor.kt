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

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        easyKeyValueRepository.increment(PickPidorAbilityCount, UserEasyKey(shopPayload.userId))
        return Phrase.PICK_PIDOR_DONE
    }
}