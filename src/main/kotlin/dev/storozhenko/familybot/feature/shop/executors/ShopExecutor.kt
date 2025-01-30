package dev.storozhenko.familybot.feature.shop.executors


import dev.storozhenko.familybot.common.extensions.from
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.shop.model.ShopItem
import org.springframework.stereotype.Component

@Component
class ShopExecutor : CommandExecutor() {

    override fun command() = Command.SHOP

    override suspend fun execute(context: ExecutorContext) {

        context.send(
            context.phrase(Phrase.SHOP_KEYBOARD),
            replyToUpdate = true,
        ) {
            keyboard {
                ShopItem.entries.forEach { item ->
                    row { button(formatLine(context, item)) { item.name } }
                }
            }
        }
    }

    private fun formatLine(
        context: ExecutorContext,
        shopItem: ShopItem,
    ): String {
        val isPremium = context.update.from().isPremium ?: false
        val additionalCost = if (isPremium) 5 else 0
        return context.phrase(shopItem.title) + " - ${shopItem.price + additionalCost}⭐"
    }
}
