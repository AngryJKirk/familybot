package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.AutoPidorTimesLeft
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.talking.Dictionary

@Component
class AutoPidorPaymentProcessor(
    private val keyValueService: EasyKeyValueService,
    private val dictionary: Dictionary
) : PaymentProcessor {
    override fun itemType() = ShopItem.AUTO_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? = null

    override fun processSuccess(shopPayload: ShopPayload): Pair<Phrase, String?> {
        val chatKey = shopPayload.chatKey()
        val autoPidorLeft = keyValueService.get(AutoPidorTimesLeft, chatKey, defaultValue = 0)
        val value = autoPidorLeft + 30
        keyValueService.put(AutoPidorTimesLeft, chatKey, value)
        val balanceComment = dictionary.get(Phrase.AUTO_PIDOR_TIMES_LEFT, chatKey) + "$value"
        return Phrase.AUTO_PIDOR_SUCCESS to balanceComment
    }
}