package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.shop.SuccessPaymentResponse
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.AutoPidorTimesLeft
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.talking.Dictionary

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
