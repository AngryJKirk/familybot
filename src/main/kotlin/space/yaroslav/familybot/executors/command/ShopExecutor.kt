package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.ShopItem
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.PaymentConfig

@Component
class ShopExecutor(
    private val dictionary: Dictionary,
    paymentConfig: PaymentConfig,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    private val isEnabled = paymentConfig.token != null

    override fun command() = Command.SHOP

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        update.toChat()
        if (isEnabled.not()) {
            return { sender ->
                sender.send(update, context.get(Phrase.SHOP_DISABLED))
            }
        }

        return {
            it.send(
                update,
                dictionary.get(Phrase.SHOP_KEYBOARD, update),
                replyToUpdate = true,
                customization = customization(context)
            )
        }
    }

    private fun customization(dictionaryContext: DictionaryContext): SendMessage.() -> Unit {
        return {
            replyMarkup = ShopItem.toKeyBoard(dictionaryContext)
        }
    }
}