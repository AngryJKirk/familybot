package dev.storozhenko.familybot.feature.shop.services.processors

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.PidorTolerance
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class PidorLimitPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
) : PaymentProcessor {
    private val log = KotlinLogging.logger { }
    override fun itemType() = ShopItem.DROP_PIDOR_LIMIT

    override fun preCheckOut(shopPayload: ShopPayload): PreCheckOutResponse {
        val pidorTolerance = easyKeyValueService.get(PidorTolerance, shopPayload.chatKey())
        log.info { "Doing pre checkout, shopPayload=$shopPayload, result is $pidorTolerance" }

        return if (pidorTolerance == null || pidorTolerance == 0L) {
            PreCheckOutResponse.Error(Phrase.DROP_PIDOR_LIMIT_INVALID)
        } else {
            PreCheckOutResponse.Success()
        }
    }

    override fun processSuccess(shopPayload: ShopPayload, rawUpdate: Update): SuccessPaymentResponse {
        easyKeyValueService.remove(PidorTolerance, shopPayload.chatKey())
        log.info { "Removed pidor limit for $shopPayload" }
        return SuccessPaymentResponse(Phrase.DROP_PIDOR_LIMIT_DONE)
    }
}
