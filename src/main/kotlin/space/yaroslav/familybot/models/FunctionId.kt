package space.yaroslav.familybot.models

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.utils.toEmoji

enum class FunctionId(val id: Int, val desc: String) {
    HUIFICATE(1, "Хуификация"),
    CHATTING(2, "Влезание в диалог"),
    PIDOR(3, "Пидор-детектор"),
    RAGE(4, "Рейдж-мод"),
    ANTIDDOS(5, "Антиспам команд"),
    ASK_WORLD(6, "Вопросы миру"),
    GREETINGS(7, "Приветствия и прощания"),
    TALK_BACK(8, "Реакция на обращения");

    companion object {
        fun toKeyBoard(isEnabled: (FunctionId) -> Boolean): InlineKeyboardMarkup {
            return InlineKeyboardMarkup(
                (
                    values()
                        .toList()
                        .map { it to isEnabled(it) }
                        .map { (function, value) -> function.desc to value }
                        .map { (description, value) -> description to value.toEmoji() }
                        .map { (description, value) ->
                            InlineKeyboardButton("$description $value").apply {
                                callbackData = description
                            }
                        }
                        .chunked(2)
                    )
            )
        }
    }
}
