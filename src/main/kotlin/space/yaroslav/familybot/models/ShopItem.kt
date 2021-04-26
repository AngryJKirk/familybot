package space.yaroslav.familybot.models

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.services.talking.DictionaryContext

enum class ShopItem(val title: Phrase, val description: Phrase, val price: Int) {

    DROP_PIDOR_LIMIT(Phrase.DROP_PIDOR_LIMIT_TITLE, Phrase.DROP_PIDOR_LIMIT_DESC, 100 * 100),
    DROP_ASK_WORLD_LIMIT(Phrase.DROP_ASK_WORLD_LIMIT_TITLE, Phrase.DROP_ASK_WORLD_LIMIT_DESC, 200 * 100),
    DROP_BET_LIMIT(Phrase.DROP_BET_LIMIT_TITLE, Phrase.DROP_BET_LIMIT_DESC, 200 * 100),
    DROP_PIDOR(Phrase.DROP_PIDOR_TITLE, Phrase.DROP_PIDOR_DESC, 200 * 100);

    companion object {
        fun toKeyBoard(dictionaryContext: DictionaryContext): InlineKeyboardMarkup {
            return InlineKeyboardMarkup(
                values()
                    .map { shopItem ->
                        InlineKeyboardButton(formatLine(dictionaryContext, shopItem))
                            .apply {
                                callbackData = shopItem.name
                            }
                    }
                    .chunked(1)

            )
        }

        private fun formatLine(
            dictionaryContext: DictionaryContext,
            shopItem: ShopItem
        ) = dictionaryContext.get(shopItem.title) + " — ${shopItem.price / 100}₽"
    }
}