package space.yaroslav.familybot.models

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.utils.toEmoji
import space.yaroslav.familybot.services.settings.BooleanSetting

object Huificate : BooleanSetting()
object Chatting : BooleanSetting()
object Pidor : BooleanSetting()
object Rage : BooleanSetting()
object AntiDdos : BooleanSetting()
object AskWorld : BooleanSetting()
object Greetings : BooleanSetting()
object TalkBack : BooleanSetting()
object UkrainianLanguage: BooleanSetting()

enum class FunctionId(val id: Int, val desc: String, val easySetting: BooleanSetting) {
    HUIFICATE(1, "Хуификация", Huificate),
    CHATTING(2, "Влезание в диалог", Chatting),
    PIDOR(3, "Пидор-детектор", Pidor),
    RAGE(4, "Рейдж-мод", Rage),
    ANTIDDOS(5, "Антиспам команд", AntiDdos),
    ASK_WORLD(6, "Вопросы миру", AskWorld),
    GREETINGS(7, "Приветствия и прощания", Greetings),
    TALK_BACK(8, "Реакция на обращения", TalkBack);

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
