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
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class PaymentRouter(
    private val paymentService: PaymentService,
    private val commonRepository: UserRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
) {
    private val log = KotlinLogging.logger {  }

    fun proceedPreCheckoutQuery(update: Update): suspend (TelegramClient) -> Unit {
        val shopPayload = getPayload(update.preCheckoutQuery.invoicePayload)
            .copy(userId = update.from().id)
        val settingsKey = ChatEasyKey(shopPayload.chatId)
        val chatId = shopPayload.chatId.toString()
        return { client ->
            runCatching { paymentService.processPreCheckoutCheck(shopPayload) }
                .onFailure { e ->
                    log.error(e) { "Can not check pre checkout query" }
                    val message = dictionary.get(Phrase.SHOP_PRE_CHECKOUT_FAIL, settingsKey)
                    client.execute(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, false, message))
                    client.execute(SendMessage(chatId, message))
                }
                .onSuccess { response ->
                    when (response) {
                        is PreCheckOutResponse.Success -> {
                            client.execute(AnswerPreCheckoutQuery(update.preCheckoutQuery.id, true))
                            log.info { "Pre checkout query is valid" }
                        }

                        is PreCheckOutResponse.Error -> {
                            val message = dictionary.get(response.explainPhrase, settingsKey)
                            client.execute(
                                AnswerPreCheckoutQuery(
                                    update.preCheckoutQuery.id,
                                    false,
                                    message,
                                ),
                            )
                            client.execute(SendMessage(chatId, message))
                        }
                    }
                }
        }
    }

    fun proceedSuccessfulPayment(update: Update): suspend (TelegramClient) -> Unit {
        val shopPayload = getPayload(update.message.successfulPayment.invoicePayload)
        val chargeId = update.message.successfulPayment.telegramPaymentChargeId
        return { client ->
            runCatching { paymentService.processSuccessfulPayment(shopPayload, chargeId) }
                .onFailure { e ->
                    log.error(e) { "Can not process payment" }
                    onFailure(client, update, shopPayload)
                }
                .onSuccess { result ->
                    log.info { "Wow, payment!" }
                    onSuccess(client, update, result, shopPayload)
                }
        }
    }

    private fun onSuccess(
        client: TelegramClient,
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
        client.execute(SendMessage(chatId, text).apply { enableHtml(true) })
        client.execute(SendMessage(chatId, dictionary.get(successPaymentResponse.phrase, chatKey)))
        successPaymentResponse.customCall(client)
        val chat = commonRepository
            .getChatsByUser(user)
            .find { shopPayload.chatId == it.id }
            ?.name ?: "[???]"
        val additionalTax = if (update.from().isPremium == true) 10 else 0
        val message =
            "<b>+${(shopPayload.shopItem.price / 100) + additionalTax}₽</b> от ${user.getGeneralName()} из чата <b>$chat</b> за <b>${shopPayload.shopItem}</b>"
        client.execute(
            SendMessage(developerId, message).apply {
                enableHtml(true)
            },
        )
    }

    private fun onFailure(
        client: TelegramClient,
        update: Update,
        shopPayload: ShopPayload,
    ) {
        val developerId = botConfig.developerId
        val text = dictionary.get(Phrase.SHOP_ERROR, update.toChat().key()).replace("$1", "@" + botConfig.developer)
        client.execute(SendMessage(shopPayload.chatId.toString(), text))

        client.execute(SendMessage(developerId, "Payment gone wrong: $update"))
    }

    private fun getPayload(invoicePayload: String): ShopPayload {
        return invoicePayload.parseJson()
    }
}
