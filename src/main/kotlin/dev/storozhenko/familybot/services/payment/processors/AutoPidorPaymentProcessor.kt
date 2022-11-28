package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.AutoPidorTimesLeft
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.talking.Dictionary

@Component
class AutoPidorPaymentProcessor(
    private val keyValueService: EasyKeyValueService,
    private val dictionary: Dictionary
) : PaymentProcessor {
    override fun itemType() = ShopItem.AUTO_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse = PreCheckOutResponse.Success()

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        val chatKey = shopPayload.chatKey()
        val autoPidorLeft = keyValueService.get(AutoPidorTimesLeft, chatKey, defaultValue = 0)
        val value = autoPidorLeft + 30
        keyValueService.put(AutoPidorTimesLeft, chatKey, value)
        val balanceComment = dictionary.get(Phrase.AUTO_PIDOR_TIMES_LEFT, chatKey) + "$value"
        return SuccessPaymentResponse(Phrase.AUTO_PIDOR_SUCCESS) {
            it.execute(SendMessage(chatKey.chatId.toString(), balanceComment))
        }
    }
}
