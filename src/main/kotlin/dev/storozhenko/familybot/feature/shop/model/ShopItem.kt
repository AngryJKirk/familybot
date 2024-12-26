package dev.storozhenko.familybot.feature.shop.model

import dev.storozhenko.familybot.common.extensions.rubles
import dev.storozhenko.familybot.core.models.dictionary.Phrase

enum class ShopItem(val title: Phrase, val description: Phrase, val price: Int) {

    DROP_PIDOR_LIMIT(Phrase.DROP_PIDOR_LIMIT_TITLE, Phrase.DROP_PIDOR_LIMIT_DESC, 80.rubles()),
    DROP_BET_LIMIT(Phrase.DROP_BET_LIMIT_TITLE, Phrase.DROP_BET_LIMIT_DESC, 100.rubles()),
    DIVORCE(Phrase.DIVORCE_TITLE, Phrase.DIVORCE_DESC, 125.rubles()),
    DROP_PIDOR(Phrase.DROP_PIDOR_TITLE, Phrase.DROP_PIDOR_DESC, 150.rubles()),
    DROP_ASK_WORLD_LIMIT(Phrase.DROP_ASK_WORLD_LIMIT_TITLE, Phrase.DROP_ASK_WORLD_LIMIT_DESC, 200.rubles()),
    PICK_PIDOR(Phrase.PICK_PIDOR_TITLE, Phrase.PICK_PIDOR_DESC, 210.rubles()),
    AUTO_PIDOR(Phrase.AUTO_PIDOR_TITLE, Phrase.AUTO_PIDOR_DESC, 250.rubles()),
    CHAT_GPT(Phrase.CHAT_GTP_TITLE, Phrase.CHAT_GTP_DESC, 300.rubles()),
    I_AM_RICH(Phrase.I_AM_RICH_TITLE, Phrase.I_AM_RICH_DESC, 1000.rubles());


}
