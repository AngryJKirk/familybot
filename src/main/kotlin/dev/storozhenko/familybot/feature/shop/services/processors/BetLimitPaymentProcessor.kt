package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import dev.storozhenko.familybot.feature.settings.models.BetTolerance
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import org.springframework.stereotype.Component

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
