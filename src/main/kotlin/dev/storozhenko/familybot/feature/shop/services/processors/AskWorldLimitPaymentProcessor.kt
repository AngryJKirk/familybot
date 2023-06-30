package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.AskWorldChatUsages
import dev.storozhenko.familybot.feature.settings.models.AskWorldUserUsages
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component

@Component
class AskWorldLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_ASK_WORLD_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val chatUsages = easyKeyValueService.get(AskWorldChatUsages, shopPayload.chatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $chatUsages")
        return if (chatUsages == null || chatUsages == 0L) {
            PreCheckOutResponse.Error(Phrase.DROP_ASK_WORLD_LIMIT_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        easyKeyValueService.remove(AskWorldChatUsages, shopPayload.chatKey())
        easyKeyValueService.remove(AskWorldUserUsages, shopPayload.userKey())
        log.info("Removed ask world keys for $shopPayload")
        return SuccessPaymentResponse(Phrase.DROP_ASK_WORLD_LIMIT_DONE)
    }
}
