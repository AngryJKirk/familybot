package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor

@Component
class IAmRichPaymentProcessor : PaymentProcessor {
    override fun itemType() = ShopItem.I_AM_RICH

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? = null

    override fun processSuccess(shopPayload: ShopPayload): Pair<Phrase, String?> {
        return Phrase.I_AM_RICH_DONE to null
    }
}
