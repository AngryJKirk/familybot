package space.yaroslav.familybot.services.payment

import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload

interface PaymentProcessor {

    fun itemType(): ShopItem

    fun preCheckOut(shopPayload: ShopPayload): Phrase?

    fun processSuccess(shopPayload: ShopPayload): Pair<Phrase, String?>
}
