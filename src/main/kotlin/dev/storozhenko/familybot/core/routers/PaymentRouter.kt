package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.common.extensions.key
import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.settings.models.PaymentKey
import dev.storozhenko.familybot.feature.shop.model.PreCheckOutResponse
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import dev.storozhenko.familybot.feature.shop.model.SuccessPaymentResponse
import dev.storozhenko.familybot.feature.shop.services.PaymentService
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.UUID

@Component
class PaymentRouter(
    private val paymentService: PaymentService,
    private val commonRepository: UserRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig,
    private val easyKeyValueService: EasyKeyValueService
) {
    private val log = KotlinLogging.logger { }

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
        return { client ->
            runCatching { paymentService.processSuccessfulPayment(shopPayload) }
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

    // todo, there is a bug in the telegram lib
    private class Refund(
        val user_id: Long,
        val telegram_payment_charge_id: String
    ) : BotApiMethodBoolean() {
        override fun getMethod() = "refundStarPayment"
    }

    fun proceedRefund(update: Update): suspend (TelegramClient) -> Unit {
        val callbackQuery = update.callbackQuery
        val paymentId = callbackQuery.data.split("=")[1]
        val refundJson = easyKeyValueService.get(PaymentKey, PlainKey(paymentId))
            ?: throw FamilyBot.InternalException("Can't find a key, update is $update")
        val refundData = refundJson.parseJson<RefundData>()
        return { client ->
            client.execute(AnswerCallbackQuery(callbackQuery.id))
            val result = runCatching { client.execute(Refund(refundData.userId, refundData.chargeId)) }
                .onFailure { e -> log.error(e) { "Can't refund due to error" } }
                .getOrDefault(false)
            client.execute(
                SendMessage(
                    botConfig.developerId,
                    "Trying to refund ${refundData.amount}⭐: ${result.toEmoji()}"
                )
            )
        }
    }

    private fun onSuccess(
        client: TelegramClient,
        update: Update,
        successPaymentResponse: SuccessPaymentResponse,
        shopPayload: ShopPayload,
    ) {
        val user = update.toUser()
        val chatKey = update.toChat().key()
        val text = dictionary.get(Phrase.SHOP_THANKS, chatKey)
            .replace("$0", user.getGeneralName())
            .replace("$1", "@" + botConfig.developer)
        val chatId = shopPayload.chatId.toString()
        client.execute(SendMessage(chatId, text).apply { enableHtml(true) })
        client.execute(SendMessage(chatId, dictionary.get(successPaymentResponse.phrase, chatKey)))
        successPaymentResponse.customCall(client)
        notifyDeveloper(client, update, shopPayload)
    }

    private data class RefundData(
        val userId: Long,
        val chargeId: String,
        val amount: Int,
    )

    private fun notifyDeveloper(client: TelegramClient, update: Update, shopPayload: ShopPayload) {
        val developerId = botConfig.developerId
        val successfulPayment = update.message.successfulPayment
        val user = update.toUser()
        val chat = commonRepository.getChatsByUser(user).find { shopPayload.chatId == it.id }?.name ?: "[хуй знает чат]"
        val message =
            "<b>+${successfulPayment.totalAmount}⭐</b> от ${user.getGeneralName()} из чата <b>$chat</b> за <b>${shopPayload.shopItem}</b>"

        val paymentKey = UUID.randomUUID().toString()
        val refundJson = RefundData(
            user.id,
            successfulPayment.telegramPaymentChargeId,
            successfulPayment.totalAmount
        ).toJson()
        easyKeyValueService.put(PaymentKey, PlainKey(paymentKey), refundJson)

        val refundButton = InlineKeyboardButton("Refund⭐").apply {
            callbackData = "REFUND=$paymentKey"
        }
        client.execute(
            SendMessage(developerId, message).apply {
                enableHtml(true)
                replyMarkup = InlineKeyboardMarkup(listOf(InlineKeyboardRow(refundButton)))
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
