package space.yaroslav.familybot.executors.continious

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.chatId
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.PaymentConfig

@Component
class ShopContiniousExecutor(
    private val paymentConfig: PaymentConfig,
    private val dictionary: Dictionary,
    botConfig: BotConfig
) : ContiniousConversation(botConfig) {
    private val objectMapper = ObjectMapper()

    override fun getDialogMessage(message: Message): String {
        return dictionary.get(Phrase.SHOP_KEYBOARD, message.chat.toChat().key())
    }

    override fun command() = Command.SHOP

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val providerToken = paymentConfig.token ?: return {}
        val chat = update.toChat()
        val context = dictionary.createContext(update)
        val callbackQuery = update.callbackQuery
        val shopItem = ShopItem.values().find { item -> callbackQuery.data == item.name }
            ?: return {}

        return {
            it.execute(AnswerCallbackQuery(callbackQuery.id))
            it.execute(
                SendInvoice(
                    chat.idString,
                    context.get(shopItem.title),
                    context.get(shopItem.description),
                    createPayload(update, shopItem),
                    providerToken,
                    "help",
                    "RUB",
                    listOf(LabeledPrice(context.get(Phrase.SHOP_PAY_LABEL), shopItem.price))
                ).apply {
                    maxTipAmount = 500
                    suggestedTipAmounts = listOf(100, 200, 300, 500)
                }
            )
        }
    }

    private fun createPayload(update: Update, shopItem: ShopItem): String {
        val payload = ShopPayload(
            update.chatId(),
            update.toUser().id,
            shopItem
        )

        return objectMapper.writeValueAsString(payload)
    }
}