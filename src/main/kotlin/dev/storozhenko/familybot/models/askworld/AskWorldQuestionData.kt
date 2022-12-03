package dev.storozhenko.familybot.models.askworld

import dev.storozhenko.familybot.models.telegram.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

sealed interface AskWorldQuestionData

class Success(
    val questionTitle: String,
    val isScam: Boolean,
    val action: suspend (AbsSender, Chat, Chat) -> Message
) : AskWorldQuestionData

class ValidationError(
    val invalidQuestionAction: suspend (AbsSender) -> Unit
) : AskWorldQuestionData
