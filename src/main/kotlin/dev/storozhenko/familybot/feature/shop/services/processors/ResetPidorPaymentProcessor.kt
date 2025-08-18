package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.common.extensions.isToday
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.startOfDay
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.settings.models.PidorTolerance
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ResetPidorPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
    private val pidorRepository: PidorRepository,
) : PaymentProcessor {
    private val log = KotlinLogging.logger { }
    override fun itemType() = ShopItem.DROP_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val chat = Chat(shopPayload.chatId, null)
        val isNonePidorToday = pidorRepository
            .getPidorsByChat(chat)
            .none { pidor -> pidor.date.isToday() }
        log.info { "Doing pre checkout, shopPayload=$shopPayload, isNonePidorsToday is $isNonePidorToday" }
        return if (isNonePidorToday) {
            PreCheckOutResponse.Error(Phrase.DROP_PIDOR_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload, rawUpdate: Update): SuccessPaymentResponse {
        val chat = Chat(shopPayload.chatId, null)
        val now = Instant.now()
        val amountOfRemovedPidors = pidorRepository.removePidorRecords(
            chat,
            from = now.startOfDay(),
            until = now.plus(1, ChronoUnit.DAYS).startOfDay(),
        )
        easyKeyValueService.remove(PidorTolerance, chat.key())
        log.info { "Removed $amountOfRemovedPidors pidors for $shopPayload" }
        return SuccessPaymentResponse(Phrase.DROP_PIDOR_DONE)
    }
}
