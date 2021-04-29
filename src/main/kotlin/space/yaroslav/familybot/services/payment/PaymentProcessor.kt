package space.yaroslav.familybot.services.payment

import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.ShopItem
import space.yaroslav.familybot.models.ShopPayload

interface PaymentProcessor {

    fun itemType(): ShopItem

    fun preCheckOut(shopPayload: ShopPayload): Phrase?

    fun processSuccess(shopPayload: ShopPayload): Phrase
}