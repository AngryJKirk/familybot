package space.yaroslav.familybot.models.router

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.extensions.toEmoji
import space.yaroslav.familybot.services.settings.BooleanKeyType
import space.yaroslav.familybot.services.settings.ChatEasyKey

abstract class ChatFunctionKeyType : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object Huificate : ChatFunctionKeyType()
object Chatting : ChatFunctionKeyType()
object Pidor : ChatFunctionKeyType()
object Rage : ChatFunctionKeyType()
object AntiDdos : ChatFunctionKeyType()
object AskWorld : ChatFunctionKeyType()
object Greetings : ChatFunctionKeyType()
object TalkBack : ChatFunctionKeyType()

enum class FunctionId(val id: Int, val desc: String, val easySetting: ChatFunctionKeyType) {
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
                        .chunked(1)
                    )
            )
        }
    }
}
