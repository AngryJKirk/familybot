package dev.storozhenko.familybot.services.payment.processors

import dev.storozhenko.familybot.common.extensions.prettyFormat
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.ChatGPTPaidTill
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.time.Duration
import java.time.Instant

@Component
class ChatGptPaymentProcessor(private val easyKeyValueService: EasyKeyValueService) : PaymentProcessor {
    override fun itemType() = ShopItem.CHAT_GPT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        return PreCheckOutResponse.Success()
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        val key = ChatEasyKey(shopPayload.chatId)
        val paidTill = Instant.ofEpochSecond(easyKeyValueService.get(ChatGPTPaidTill, key, Instant.now().epochSecond))
        if (paidTill.isBefore(Instant.now())) {
            easyKeyValueService.put(ChatGPTPaidTill, key, Instant.now().epochSecond + Duration.ofDays(30).seconds)
        } else {
            easyKeyValueService.put(ChatGPTPaidTill, key, paidTill.epochSecond + Duration.ofDays(30).seconds)
        }
        val date = Instant.ofEpochSecond(easyKeyValueService.get(ChatGPTPaidTill, key, 0)).prettyFormat()
        return SuccessPaymentResponse(Phrase.CHAT_GTP_SUCCESS) {
            it.execute(SendMessage(key.chatId.toString(), "Оплачено до $date"))
        }
    }
}