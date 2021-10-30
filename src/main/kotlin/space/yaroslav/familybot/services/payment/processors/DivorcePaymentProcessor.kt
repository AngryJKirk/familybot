package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.repos.MarriagesRepository
import space.yaroslav.familybot.services.payment.PaymentProcessor

@Component
class DivorcePaymentProcessor(
    private val marriagesRepository: MarriagesRepository
) : PaymentProcessor {
    override fun itemType() = ShopItem.DIVORCE

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? {
        val marriage = marriagesRepository.getMarriage(shopPayload.chatId, shopPayload.userId)
        return if (marriage != null) {
            null
        } else {
            Phrase.DIVORCE_PRE_CHECKOUT_MARRIAGE_NOT_FOUND
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        marriagesRepository.removeMarriage(shopPayload.chatId, shopPayload.userId)
        return Phrase.MARRY_DIVORCE
    }
}
