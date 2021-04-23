package space.yaroslav.familybot.models

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.utils.toEmoji
import space.yaroslav.familybot.services.settings.BooleanSetting
import space.yaroslav.familybot.services.settings.ChatSettingsKey

abstract class ChatFunctionSetting : BooleanSetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object Huificate : ChatFunctionSetting()
object Chatting : ChatFunctionSetting()
object Pidor : ChatFunctionSetting()
object Rage : ChatFunctionSetting()
object AntiDdos : ChatFunctionSetting()
object AskWorld : ChatFunctionSetting()
object Greetings : ChatFunctionSetting()
object TalkBack : ChatFunctionSetting()

enum class FunctionId(val id: Int, val desc: String, val easySetting: ChatFunctionSetting) {
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
