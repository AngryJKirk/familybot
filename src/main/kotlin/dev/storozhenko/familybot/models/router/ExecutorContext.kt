package dev.storozhenko.familybot.models.router

import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.UserAndChatEasyKey
import dev.storozhenko.familybot.services.settings.UserEasyKey
import dev.storozhenko.familybot.services.talking.Dictionary
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

class ExecutorContext(
    val update: Update,
    val message: Message,
    val command: Command?,
    val isFromDeveloper: Boolean,
    val chat: Chat,
    val user: User,
    val userAndChatKey: UserAndChatEasyKey,
    val userKey: UserEasyKey,
    val chatKey: ChatEasyKey,
    val testEnvironment: Boolean,
    private val dictionary: Dictionary
) {
    fun phrase(phrase: Phrase) = dictionary.get(phrase, chatKey)
    fun allPhrases(phrase: Phrase) = dictionary.getAll(phrase)
}
