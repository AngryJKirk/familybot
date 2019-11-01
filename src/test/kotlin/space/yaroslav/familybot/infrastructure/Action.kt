package space.yaroslav.familybot.infrastructure

import org.telegram.telegrambots.meta.api.objects.InputFile

sealed class Action<T>(
    val chatId: String,
    val isHtmlEnabled: Boolean,
    val replyId: Int?,
    val content: T
)

class ActionWithText(chatId: String, isHtmlEnabled: Boolean = false, replyId: Int? = null, content: String) :
    Action<String>(chatId, isHtmlEnabled, replyId, content) {
    override fun toString(): String {
        return content
    }
}

class ActionWithSticker(chatId: String, isHtmlEnabled: Boolean = false, replyId: Int? = null, content: InputFile) :
    Action<InputFile>(chatId, isHtmlEnabled, replyId, content)
