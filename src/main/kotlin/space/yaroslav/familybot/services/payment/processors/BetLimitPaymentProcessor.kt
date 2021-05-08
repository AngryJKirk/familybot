package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.BetTolerance
import space.yaroslav.familybot.services.settings.EasyKeyValueService

@Component
class BetLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_BET_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? {
        val betTolerance = easyKeyValueService.get(BetTolerance, shopPayload.userAndChatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $betTolerance")
        return if (betTolerance == null || betTolerance == false) {
            Phrase.DROP_BET_LIMIT_INVALID
        } else {
            null
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        easyKeyValueService.remove(BetTolerance, shopPayload.userAndChatKey())
        log.info("Removed bet limit for $shopPayload")
        return Phrase.DROP_BET_LIMIT_DONE
    }
}
