package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorTolerance

@Component
class PidorLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_PIDOR_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? {
        val pidorTolerance = easyKeyValueService.get(PidorTolerance, shopPayload.chatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $pidorTolerance")

        return if (pidorTolerance == null || pidorTolerance == 0L) {
            Phrase.DROP_PIDOR_LIMIT_INVALID
        } else {
            null
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): Pair<Phrase, String?> {
        easyKeyValueService.remove(PidorTolerance, shopPayload.chatKey())
        log.info("Removed pidor limit for $shopPayload")
        return Phrase.DROP_PIDOR_LIMIT_DONE to null
    }
}
