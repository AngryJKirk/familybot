package space.yaroslav.familybot.services.routers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.payment.PaymentService
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PaymentRouter(
    private val paymentService: PaymentService,
    private val commonRepository: CommonRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) {
    private val log = getLogger()
    private val objectMapper = jacksonObjectMapper()

    fun proceedPreCheckoutQuery(update: Update): suspend (AbsSender) -> Unit {
        val shopPayload = getPayload(update.preCheckoutQuery.invoicePayload)

        val settingsKey = ChatEasyKey(shopPayload.chatId)
        val chatId = shopPayload.chatId.toString()
        return { sender ->
            runCatching { paymentService.processPreCheckoutCheck(shopPayload) }
                .onFailure { e ->
                    log.error("Can not check pre checkout query", e)
                    val message = dictionary.get(Phrase.SHOP_PRE_CHECKOUT_FAIL, settingsKey)
                    sender.execute(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, false, message))
                    sender.execute(SendMessage(chatId, message))
                }
                .onSuccess { phrase ->
                    if (phrase != null) {
                        val message = dictionary.get(phrase, settingsKey)
                        sender.execute(
                            AnswerPreCheckoutQuery(
                                update.preCheckoutQuery.id,
                                false,
                                message
                            )
                        )
                        sender.execute(SendMessage(chatId, message))
                    } else {
                        sender.execute(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, true))
                        log.info("Pre checkout query is valid")
                    }
                }
        }
    }

    fun proceedSuccessfulPayment(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val shopPayload = getPayload(update.message.successfulPayment.invoicePayload)
        return { sender ->
            runCatching { paymentService.processSuccessfulPayment(shopPayload) }
                .onFailure { e ->
                    log.error("Can not process payment", e)
                    onFailure(sender, update, context, shopPayload)
                }
                .onSuccess { phrase ->
                    log.info("Wow, payment!")
                    onSuccess(context, sender, update, phrase, shopPayload)
                }
        }
    }

    private fun onSuccess(
        context: DictionaryContext,
        sender: AbsSender,
        update: Update,
        phrase: Phrase,
        shopPayload: ShopPayload
    ) {
        val developerId = botConfig.developerId
        val text = context.get(Phrase.SHOP_THANKS).replace("$1", "@" + botConfig.developer)
        val chatId = shopPayload.chatId.toString()
        sender.execute(SendMessage(chatId, text))
        sender.execute(SendMessage(chatId, context.get(phrase)))
        if (developerId != null) {
            val user = update.toUser()
            val chat = commonRepository
                .getChatsByUser(user)
                .find { shopPayload.chatId == it.id }
                ?.name ?: "[???]"
            val message =
                "+${shopPayload.shopItem.price / 100}₽ от ${user.getGeneralName()} из чата $chat за ${shopPayload.shopItem}"
            sender.execute(SendMessage(developerId, message))
        } else {
            log.warn("Developer ID is not set, can not send successful payment, so logging")
            log.warn("Successful payment: $update")
        }
    }

    private fun onFailure(
        sender: AbsSender,
        update: Update,
        context: DictionaryContext,
        shopPayload: ShopPayload
    ) {
        val developerId = botConfig.developerId
        val text = context.get(Phrase.SHOP_ERROR).replace("$1", "@" + botConfig.developer)
        sender.execute(SendMessage(shopPayload.chatId.toString(), text))

        if (developerId != null) {
            sender.execute(SendMessage(developerId, "Payment gone wrong: $update"))
        } else {
            log.error("Developer ID is not set, can not send error payment, so logging")
            log.error("Error payment: $update")
        }
    }

    private fun getPayload(invoicePayload: String): ShopPayload {
        return objectMapper.readValue(invoicePayload)
    }
}
