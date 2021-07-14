package space.yaroslav.familybot.services.payment

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload

@Component
class IAmRichPaymentProcessor : PaymentProcessor {
    override fun itemType() = ShopItem.I_AM_RICH

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? = null

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        return Phrase.I_AM_RICH_DONE
    }
}