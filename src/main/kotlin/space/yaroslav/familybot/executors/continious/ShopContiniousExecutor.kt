package space.yaroslav.familybot.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.rubles
import space.yaroslav.familybot.common.extensions.toJson
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.shop.ShopPayload
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ShopContiniousExecutor(
    private val botConfig: BotConfig,
) : ContiniousConversationExecutor(botConfig) {

    override fun getDialogMessage(context: ExecutorContext): String {
        return context.phrase(Phrase.SHOP_KEYBOARD)
    }

    override fun command() = Command.SHOP

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val providerToken = botConfig.paymentToken ?: return {}
        val chat = context.chat

        val callbackQuery = context.update.callbackQuery
        val shopItem = ShopItem.values().find { item -> callbackQuery.data == item.name }
            ?: return {}

        return {
            it.execute(AnswerCallbackQuery(callbackQuery.id))
            it.execute(
                SendInvoice(
                    chat.idString,
                    context.phrase(shopItem.title),
                    context.phrase(shopItem.description),
                    createPayload(context, shopItem),
                    providerToken,
                    "help",
                    "RUB",
                    listOf(LabeledPrice(context.phrase(Phrase.SHOP_PAY_LABEL), shopItem.price))
                ).apply {
                    maxTipAmount = 100.rubles()
                    suggestedTipAmounts = listOf(10.rubles(), 20.rubles(), 50.rubles(), 100.rubles())
                }
            )
        }
    }

    private fun createPayload(context: ExecutorContext, shopItem: ShopItem): String {
        return ShopPayload(
            context.chat.id,
            context.user.id,
            shopItem
        ).toJson()
    }
}
