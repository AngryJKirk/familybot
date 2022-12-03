package dev.storozhenko.familybot.services.payment.processors

import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.AskWorldChatUsages
import dev.storozhenko.familybot.services.settings.AskWorldUserUsages
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import org.springframework.stereotype.Component

@Component
class AskWorldLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
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
