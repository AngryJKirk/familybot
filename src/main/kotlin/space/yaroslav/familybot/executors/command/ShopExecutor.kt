package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.shop.ShopItem
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ShopExecutor(
    botConfig: BotConfig
) : CommandExecutor() {
    private val isEnabled = botConfig.paymentToken != null

    override fun command() = Command.SHOP

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        if (isEnabled.not()) {
            return { sender ->
                sender.send(executorContext, executorContext.phrase(Phrase.SHOP_DISABLED))
            }
        }

        return {
            it.send(
                executorContext,
                executorContext.phrase(Phrase.SHOP_KEYBOARD),
                replyToUpdate = true,
                customization = customization(executorContext)
            )
        }
    }

    private fun customization(executorContext: ExecutorContext): SendMessage.() -> Unit {
        return {
            replyMarkup = ShopItem.toKeyBoard(executorContext)
        }
    }
}
