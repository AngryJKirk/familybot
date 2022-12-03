package dev.storozhenko.familybot.services.payment.processors

import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.PidorTolerance
import org.springframework.stereotype.Component

@Component
class PidorLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_PIDOR_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val pidorTolerance = easyKeyValueService.get(PidorTolerance, shopPayload.chatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $pidorTolerance")

        return if (pidorTolerance == null || pidorTolerance == 0L) {
            PreCheckOutResponse.Error(Phrase.DROP_PIDOR_LIMIT_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        easyKeyValueService.remove(PidorTolerance, shopPayload.chatKey())
        log.info("Removed pidor limit for $shopPayload")
        return SuccessPaymentResponse(Phrase.DROP_PIDOR_LIMIT_DONE)
    }
}
