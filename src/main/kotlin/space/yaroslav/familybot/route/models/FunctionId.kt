package space.yaroslav.familybot.route.models

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import space.yaroslav.familybot.common.utils.toEmoji
import space.yaroslav.familybot.common.utils.toInlineKeyBoard


enum class FunctionId(val id: Int, val desc: String) {
    HUIFICATE(1, "Хуификация"),
    CHATTING(2, "Влезание в диалог"),
    PIDOR(3, "Пидор-детектор"),
    RAGE(4, "Рейдж-мод"),
    ANTIDDOS(5, "Антиспам команд"),
    ASK_WORLD(6, "Вопросы миру");

    companion object {
        fun toKeyBoard(isEnabled: (FunctionId) -> Boolean): InlineKeyboardMarkup {
            return InlineKeyboardMarkup().setKeyboard(FunctionId.values()
                    .toList()
                    .map { it to isEnabled(it) }
                    .map { it.first.desc to it.second }
                    .map { it.first to it.second.toEmoji() }
                    .toInlineKeyBoard())
        }
    }
}