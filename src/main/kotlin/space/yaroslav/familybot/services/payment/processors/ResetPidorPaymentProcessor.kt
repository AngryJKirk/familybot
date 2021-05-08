package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.extensions.isToday
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.startOfDay
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorTolerance
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ResetPidorPaymentProcessor(
    private val easyKeyValueService: EasyKeyValueService,
    private val commonRepository: CommonRepository
) : PaymentProcessor {
    private val log = getLogger()
    override fun itemType() = ShopItem.DROP_PIDOR

    override fun preCheckOut(shopPayload: ShopPayload): Phrase? {

        val chat = Chat(shopPayload.chatId, null)
        val isNonePidorToday = commonRepository
            .getPidorsByChat(chat)
            .none { pidor -> pidor.date.isToday() }
        log.info("Doing pre checkout, shopPayload=$shopPayload, isNonePidorsToday is $isNonePidorToday")
        return if (isNonePidorToday) {
            Phrase.DROP_PIDOR_INVALID
        } else {
            null
        }
    }

    override fun processSuccess(shopPayload: ShopPayload): Phrase {
        val chat = Chat(shopPayload.chatId, null)
        val now = Instant.now()
        val amountOfRemovedPidors = commonRepository.removePidorRecords(
            chat,
            from = now.startOfDay(),
            until = now.plus(1, ChronoUnit.DAYS).startOfDay()
        )
        easyKeyValueService.remove(PidorTolerance, chat.key())
        log.info("Removed $amountOfRemovedPidors pidors for $shopPayload")
        return Phrase.DROP_PIDOR_DONE
    }
}