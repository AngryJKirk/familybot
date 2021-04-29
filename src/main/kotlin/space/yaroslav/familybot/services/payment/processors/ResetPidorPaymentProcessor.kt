package space.yaroslav.familybot.services.payment.processors

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.ShopItem
import space.yaroslav.familybot.models.ShopPayload
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.payment.PaymentProcessor
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PidorTolerance
import java.time.Instant

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
        commonRepository
            .getPidorsByChat(chat, endDate = Instant.now().plusSeconds(100))
            .filter { pidor -> pidor.date.isToday() }
            .distinctBy { pidor -> pidor.user.id }
            .forEach { pidor ->
                log.info("Removing pidor $pidor")
                val rows = commonRepository.removePidorRecord(pidor.user)
                log.info("Pidor $pidor removed, rows affected: $rows")
            }
        easyKeyValueService.remove(PidorTolerance, chat.key())
        log.info("Removed all pidors for $shopPayload")
        return Phrase.DROP_PIDOR_DONE
    }
}