package space.yaroslav.familybot.services.routers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.parseJson
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.PreCheckOutResponse
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.shop.SuccessPaymentResponse
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.payment.PaymentService
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class PaymentRouter(
    private val paymentService: PaymentService,
    private val commonRepository: CommonRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) {
    private val log = getLogger()

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
                .onSuccess { response ->
                    when (response) {
                        is PreCheckOutResponse.Success -> {
                            sender.execute(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, true))
                            log.info("Pre checkout query is valid")
                        }
                        is PreCheckOutResponse.Error -> {
                            val message = dictionary.get(response.explainPhrase, settingsKey)
                            sender.execute(
                                AnswerPreCheckoutQuery(
                                    update.preCheckoutQuery.id,
                                    false,
                                    message
                                )
                            )
                            sender.execute(SendMessage(chatId, message))
                        }
                    }
                }
        }
    }

    fun proceedSuccessfulPayment(update: Update): suspend (AbsSender) -> Unit {

        val shopPayload = getPayload(update.message.successfulPayment.invoicePayload)
        return { sender ->
            runCatching { paymentService.processSuccessfulPayment(shopPayload) }
                .onFailure { e ->
                    log.error("Can not process payment", e)
                    onFailure(sender, update, shopPayload)
                }
                .onSuccess { result ->
                    log.info("Wow, payment!")
                    onSuccess(sender, update, result, shopPayload)
                }
        }
    }

    private fun onSuccess(
        sender: AbsSender,
        update: Update,
        successPaymentResponse: SuccessPaymentResponse,
        shopPayload: ShopPayload
    ) {
        val developerId = botConfig.developerId
        val user = update.toUser()
        val chatKey = update.toChat().key()
        val text = dictionary.get(Phrase.SHOP_THANKS, chatKey)
            .replace("$0", user.getGeneralName())
            .replace("$1", "@" + botConfig.developer)
        val chatId = shopPayload.chatId.toString()
        sender.execute(SendMessage(chatId, text))
        sender.execute(SendMessage(chatId, dictionary.get(successPaymentResponse.phrase, chatKey)))
        successPaymentResponse.customCall(sender)
        val chat = commonRepository
            .getChatsByUser(user)
            .find { shopPayload.chatId == it.id }
            ?.name ?: "[???]"
        val message =
            "<b>+${shopPayload.shopItem.price / 100}₽</b> от ${user.getGeneralName()} из чата <b>$chat</b> за <b>${shopPayload.shopItem}</b>"
        sender.execute(
            SendMessage(developerId, message).apply {
                enableHtml(true)
            }
        )
    }

    private fun onFailure(
        sender: AbsSender,
        update: Update,
        shopPayload: ShopPayload
    ) {
        val developerId = botConfig.developerId
        val text = dictionary.get(Phrase.SHOP_ERROR, update.toChat().key()).replace("$1", "@" + botConfig.developer)
        sender.execute(SendMessage(shopPayload.chatId.toString(), text))

        sender.execute(SendMessage(developerId, "Payment gone wrong: $update"))
    }

    private fun getPayload(invoicePayload: String): ShopPayload {
        return invoicePayload.parseJson()
    }
}
