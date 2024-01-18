package dev.storozhenko.familybot.feature.shop.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.common.extensions.rubles
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import dev.storozhenko.familybot.feature.shop.model.ShopPayload
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice

@Component
class ShopContiniousExecutor(
    private val botConfig: BotConfig,
) : ContiniousConversationExecutor(botConfig) {

    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return context.allPhrases(Phrase.SHOP_KEYBOARD)
    }

    override fun command() = Command.SHOP

    override suspend fun execute(context: ExecutorContext) {
        val providerToken = botConfig.paymentToken ?: return
        val chat = context.chat

        val callbackQuery = context.update.callbackQuery
        val shopItem = ShopItem.entries.find { item -> callbackQuery.data == item.name }
            ?: return

        val additionalTax = if (context.update.from().isPremium == true) 10.rubles() else 0
        context.sender.execute(AnswerCallbackQuery(callbackQuery.id))
        context.sender.execute(
            SendInvoice(
                chat.idString,
                context.phrase(shopItem.title),
                context.phrase(shopItem.description),
                createPayload(context, shopItem),
                providerToken,
                "help",
                "RUB",
                listOf(LabeledPrice(context.phrase(Phrase.SHOP_PAY_LABEL), shopItem.price + additionalTax)),
            ).apply {
                maxTipAmount = 100.rubles()
                suggestedTipAmounts = listOf(10.rubles(), 20.rubles(), 50.rubles(), 100.rubles())
            },
        )
    }

    private fun createPayload(context: ExecutorContext, shopItem: ShopItem): String {
        return ShopPayload(context.chat.id, context.user.id, shopItem).toJson()
    }
}
