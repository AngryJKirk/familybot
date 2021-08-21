package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ShopExecutor(
    private val dictionary: Dictionary,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    private val isEnabled = botConfig.paymentToken != null

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
