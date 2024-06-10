package dev.storozhenko.familybot.feature.askworld.models

import dev.storozhenko.familybot.core.models.telegram.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

sealed interface AskWorldQuestionData

class Success(
    val questionTitle: String,
    val isScam: Boolean,
    val action: suspend (TelegramClient, Chat, Chat) -> Message,
) : AskWorldQuestionData

class ValidationError(
    val invalidQuestionAction: suspend (TelegramClient) -> Unit,
) : AskWorldQuestionData
