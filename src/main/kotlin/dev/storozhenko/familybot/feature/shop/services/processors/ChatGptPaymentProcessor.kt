package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.ChatGPTSummaryCooldown
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ChatGptPaymentProcessor(private val easyKeyValueService: EasyKeyValueService) : PaymentProcessor {
    override fun itemType() = ShopItem.CHAT_GPT

    override fun preCheckOut(shopPayload: ShopPayload) = PreCheckOutResponse.Success()

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        val key = ChatEasyKey(shopPayload.chatId)
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, key, Instant.now())
        val newDate = if (paidTill.isBefore(Instant.now())) {
            val value = Instant.now().plus(30, ChronoUnit.DAYS)
            easyKeyValueService.put(ChatGPTPaidTill, key, value)
            value
        } else {
            val value = paidTill.plus(30, ChronoUnit.DAYS)
            easyKeyValueService.put(ChatGPTPaidTill, key, value)
            value
        }
        easyKeyValueService.remove(ChatGPTSummaryCooldown, key)
        return SuccessPaymentResponse(Phrase.CHAT_GTP_SUCCESS) {
            it.execute(SendMessage(key.chatId.toString(), "Оплачено до $newDate"))
        }
    }
}
