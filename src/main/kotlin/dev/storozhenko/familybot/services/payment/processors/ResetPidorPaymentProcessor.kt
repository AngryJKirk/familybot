package dev.storozhenko.familybot.services.payment.processors

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.common.extensions.isToday
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.startOfDay
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.shop.PreCheckOutResponse
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.shop.ShopPayload
import dev.storozhenko.familybot.models.shop.SuccessPaymentResponse
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.repos.CommonRepository
import dev.storozhenko.familybot.services.payment.PaymentProcessor
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.PidorTolerance
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ResetPidorPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
    private val commonRepository: CommonRepository
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {

        val chat = Chat(shopPayload.chatId, null)
        val isNonePidorToday = commonRepository
            .getPidorsByChat(chat)
            .none { pidor -> pidor.date.isToday() }
        log.info("Doing pre checkout, shopPayload=$shopPayload, isNonePidorsToday is $isNonePidorToday")
        return if (isNonePidorToday) {
            PreCheckOutResponse.Error(Phrase.DROP_PIDOR_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): SuccessPaymentResponse {
        val chat = Chat(shopPayload.chatId, null)
        val now = Instant.now()
        val amountOfRemovedPidors = commonRepository.removePidorRecords(
            chat,
            from = now.startOfDay(),
            until = now.plus(1, ChronoUnit.DAYS).startOfDay()
        )
        easyKeyValueService.remove(PidorTolerance, chat.key())
        log.info("Removed $amountOfRemovedPidors pidors for $shopPayload")
        return SuccessPaymentResponse(Phrase.DROP_PIDOR_DONE)
    }
}
