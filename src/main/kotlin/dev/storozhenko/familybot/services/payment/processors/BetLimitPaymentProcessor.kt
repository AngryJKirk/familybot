package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.BetTolerance
import dev.storozhenko.familybot.services.settings.EasyKeyValueService

@Component
class BetLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_BET_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val betTolerance = easyKeyValueService.get(BetTolerance, shopPayload.userAndChatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $betTolerance")
        return if (betTolerance == null || betTolerance == false) {
            PreCheckOutResponse.Error(Phrase.DROP_BET_LIMIT_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        easyKeyValueService.remove(BetTolerance, shopPayload.userAndChatKey())
        log.info("Removed bet limit for $shopPayload")
        return SuccessPaymentResponse(Phrase.DROP_BET_LIMIT_DONE)
    }
}
