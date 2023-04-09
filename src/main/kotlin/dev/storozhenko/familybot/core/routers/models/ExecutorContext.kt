package dev.storozhenko.familybot.core.routers.models

import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

data class ExecutorContext(
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
    val sender: AbsSender,
    private val dictionary: Dictionary
) {
    fun phrase(phrase: Phrase) = dictionary.get(phrase, chatKey)
    fun allPhrases(phrase: Phrase) = dictionary.getAll(phrase)
}
