package space.yaroslav.familybot.models.shop

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.extensions.rubles
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext

enum class ShopItem(val title: Phrase, val description: Phrase, val price: Int) {

    DROP_PIDOR_LIMIT(Phrase.DROP_PIDOR_LIMIT_TITLE, Phrase.DROP_PIDOR_LIMIT_DESC, 80.rubles()),
    DROP_BET_LIMIT(Phrase.DROP_BET_LIMIT_TITLE, Phrase.DROP_BET_LIMIT_DESC, 100.rubles()),
    DIVORCE(Phrase.DIVORCE_TITLE, Phrase.DIVORCE_DESC, 125.rubles()),
    DROP_PIDOR(Phrase.DROP_PIDOR_TITLE, Phrase.DROP_PIDOR_DESC, 150.rubles()),
    DROP_ASK_WORLD_LIMIT(Phrase.DROP_ASK_WORLD_LIMIT_TITLE, Phrase.DROP_ASK_WORLD_LIMIT_DESC, 200.rubles()),
    PICK_PIDOR(Phrase.PICK_PIDOR_TITLE, Phrase.PICK_PIDOR_DESC, 210.rubles()),
    I_AM_RICH(Phrase.I_AM_RICH_TITLE, Phrase.I_AM_RICH_DESC, 5000.rubles());

    companion object {
        fun toKeyBoard(context: ExecutorContext): InlineKeyboardMarkup {
            return InlineKeyboardMarkup(
                values()
                    .map { shopItem ->
                        InlineKeyboardButton(formatLine(context, shopItem))
                            .apply {
                                callbackData = shopItem.name
                            }
                    }
                    .chunked(1)

            )
        }

        private fun formatLine(
            context: ExecutorContext,
            shopItem: ShopItem
        ) = context.phrase(shopItem.title) + " - ${shopItem.price / 100}₽"
    }
}
