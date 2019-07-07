package space.yaroslav.familybot.infrastructure

class Action(
    val text: String,
    val chatId: String,
    val replyId: Int? = null,
    val isHtmlEnabled: Boolean = false
)
