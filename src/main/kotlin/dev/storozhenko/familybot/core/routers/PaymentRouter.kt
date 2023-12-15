package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentService
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.getLogger
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class PaymentRouter(
    private val paymentService: PaymentService,
    private val commonRepository: UserRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
) {
    private val log = getLogger()

    fun proceedPreCheckoutQuery(update: Update): suspend (AbsSender) -> Unit {
        val shopPayload = getPayload(update.preCheckoutQuery.invoicePayload)
            .copy(userId = update.from().id)
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
                                    message,
                                ),
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
        shopPayload: ShopPayload,
    ) {
        val developerId = botConfig.developerId
        val user = update.toUser()
        val chatKey = update.toChat().key()
        val text = dictionary.get(Phrase.SHOP_THANKS, chatKey)
            .replace("$0", user.getGeneralName())
            .replace("$1", "@" + botConfig.developer)
        val chatId = shopPayload.chatId.toString()
        sender.execute(SendMessage(chatId, text).apply { enableHtml(true) })
        sender.execute(SendMessage(chatId, dictionary.get(successPaymentResponse.phrase, chatKey)))
        successPaymentResponse.customCall(sender)
        val chat = commonRepository
            .getChatsByUser(user)
            .find { shopPayload.chatId == it.id }
            ?.name ?: "[???]"
        val additionalTax = if (update.from().isPremium == true) 10 else 0
        val message =
            "<b>+${(shopPayload.shopItem.price / 100) + additionalTax}₽</b> от ${user.getGeneralName()} из чата <b>$chat</b> за <b>${shopPayload.shopItem}</b>"
        sender.execute(
            SendMessage(developerId, message).apply {
                enableHtml(true)
            },
        )
    }

    private fun onFailure(
        sender: AbsSender,
        update: Update,
        shopPayload: ShopPayload,
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
