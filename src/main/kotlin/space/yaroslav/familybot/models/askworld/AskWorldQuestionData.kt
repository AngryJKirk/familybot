package space.yaroslav.familybot.models.askworld

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Chat

sealed interface AskWorldQuestionData

class Success(
    val questionTitle: String,
    val isScam: Boolean,
    val action: suspend (AbsSender, Chat, Chat) -> Message
) : AskWorldQuestionData

class ValidationError(
    val invalidQuestionAction: suspend (AbsSender) -> Unit
) : AskWorldQuestionData