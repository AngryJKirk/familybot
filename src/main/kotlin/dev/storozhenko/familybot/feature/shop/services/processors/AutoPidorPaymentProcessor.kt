package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.AutoPidorTimesLeft
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class AutoPidorPaymentProcessor(
    private val keyValueService: EasyKeyValueService,
    private val dictionary: Dictionary,
) : PaymentProcessor {
    override fun itemType() = ShopItem.AUTO_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload) = PreCheckOutResponse.Success()

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
