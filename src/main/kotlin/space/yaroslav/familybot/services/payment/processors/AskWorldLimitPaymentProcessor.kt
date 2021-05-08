package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.AskWorldChatUsages
import space.yaroslav.familybot.services.settings.AskWorldUserUsages
import space.yaroslav.familybot.services.settings.EasyKeyValueService

@Component
class AskWorldLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_ASK_WORLD_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? {
        val chatUsages = easyKeyValueService.get(AskWorldChatUsages, shopPayload.chatKey())
        log.info("Doing pre checkout, shopPayload=$shopPayload, result is $chatUsages")
        return if (chatUsages == null || chatUsages == 0L) {
            Phrase.DROP_ASK_WORLD_LIMIT_INVALID
        } else {
            null
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        easyKeyValueService.remove(AskWorldChatUsages, shopPayload.chatKey())
        easyKeyValueService.remove(AskWorldUserUsages, shopPayload.userKey())
        log.info("Removed ask world keys for $shopPayload")
        return Phrase.DROP_ASK_WORLD_LIMIT_DONE
    }
}