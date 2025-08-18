package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.BetTolerance
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class BetLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : PaymentProcessor {
    private val log = KotlinLogging.logger { }
    override fun itemType() = ShopItem.DROP_BET_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val betTolerance = easyKeyValueService.get(BetTolerance, shopPayload.userAndChatKey())
        log.info { "Doing pre checkout, shopPayload=$shopPayload, result is $betTolerance" }
        return if (betTolerance == null || betTolerance == false) {
            PreCheckOutResponse.Error(Phrase.DROP_BET_LIMIT_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload, rawUpdate: Update): SuccessPaymentResponse {
        easyKeyValueService.remove(BetTolerance, UserAndChatEasyKey(rawUpdate.toUser().id, shopPayload.chatId))
        log.info { "Removed bet limit for $shopPayload" }
        return SuccessPaymentResponse(Phrase.DROP_BET_LIMIT_DONE)
    }
}
