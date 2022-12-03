package dev.storozhenko.familybot.executors.command

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.shop.ShopItem
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class ShopExecutor(
    botConfig: BotConfig
) : CommandExecutor() {
    private val isEnabled = botConfig.paymentToken != null

    override fun command() = Command.SHOP

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (isEnabled.not()) {
            return { sender ->
                sender.send(context, context.phrase(Phrase.SHOP_DISABLED))
            }
        }

        return {
            it.send(
                context,
                context.phrase(Phrase.SHOP_KEYBOARD),
                replyToUpdate = true,
                customization = customization(context)
            )
        }
    }

    private fun customization(context: ExecutorContext): SendMessage.() -> Unit {
        return {
            replyMarkup = ShopItem.toKeyBoard(context)
        }
    }
}
