package dev.storozhenko.familybot.feature.settings.models

import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.core.keyvalue.models.BooleanKeyType
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

enum class FunctionId(val id: Int, val desc: String, val easySetting: BooleanKeyType<ChatEasyKey>) {
    HUIFICATE(1, "Хуификация", Huificate),
    CHATTING(2, "Влезание в диалог", Chatting),
    PIDOR(3, "Пидор-детектор", Pidor),
    RAGE(4, "Рейдж-мод", Rage),
    ANTIDDOS(5, "Антиспам команд", AntiDdos),
    ASK_WORLD(6, "Вопросы миру", AskWorld),
    GREETINGS(7, "Приветствия и прощания", Greetings),
    TALK_BACK(8, "Реакция на обращения", TalkBack);

    object Huificate : BooleanKeyType<ChatEasyKey>
    object Chatting : BooleanKeyType<ChatEasyKey>
    object Pidor : BooleanKeyType<ChatEasyKey>
    object Rage : BooleanKeyType<ChatEasyKey>
    object AntiDdos : BooleanKeyType<ChatEasyKey>
    object AskWorld : BooleanKeyType<ChatEasyKey>
    object Greetings : BooleanKeyType<ChatEasyKey>
    object TalkBack : BooleanKeyType<ChatEasyKey>

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
                        .chunked(1)
                    ),
            )
        }
    }
}
