package dev.storozhenko.familybot.feature.shop.executors


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class ShopExecutor : CommandExecutor() {

    override fun command() = Command.SHOP

    override suspend fun execute(context: ExecutorContext) {

        context.send(
            context.phrase(Phrase.SHOP_KEYBOARD),
            replyToUpdate = true,
            customization = customization(context),
        )
    }

    private fun customization(context: ExecutorContext): SendMessage.() -> Unit {
        return {
            replyMarkup = ShopItem.toKeyBoard(context)
        }
    }
}
