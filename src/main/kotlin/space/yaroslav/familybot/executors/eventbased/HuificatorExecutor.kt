package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TalkingDensity
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern

@Component
class HuificatorExecutor(private val easyKeyValueService: EasyKeyValueService) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.HUIFICATE
    }

    override fun priority(update: Update): Priority {
        return Priority.RANDOM
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        val message = update.message
        val text = message.text ?: return {}

        if (shouldHuificate(update.toChat())) {
            val huifyed = huify(text) ?: return { }
            return { it -> it.send(update, huifyed, shouldTypeBeforeSend = true) }
        } else {
            return { }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return false
    }

    fun huify(word: String): String? {
        val wordLowerCase = getLastWord(word).toLowerCase()

        if (wordLowerCase.length < 5) {
            return null
        }
        if (english.matcher(wordLowerCase).matches()) {
            return null
        }
        if (nonLetters.matcher(wordLowerCase.dropLast(wordLowerCase.length - 3)).matches()) {
            return null
        }

        if (onlyDashes.matcher(wordLowerCase).matches()) {
            return null
        }

        if (wordLowerCase.startsWith("ху", true) || wordLowerCase.length < 5) {
            return null
        }

        val postfix = String(wordLowerCase.toCharArray().dropWhile { !vowels.contains(it) }.toCharArray())

        return if (rules.containsKey(postfix[0])) {
            "ху" + rules[postfix[0]] + postfix.drop(1).dropLastDelimiter()
        } else {
            "ху$postfix"
        }
    }

    private fun getLastWord(text: String) = text.split(regex = spaces).last()

    private fun shouldHuificate(chat: Chat): Boolean {
        val density = getTalkingDensity(chat)
        return if (density == 0L) {
            true
        } else {
            ThreadLocalRandom.current().nextLong(0, density) == 0L
        }
    }

    private fun getTalkingDensity(chat: Chat): Long {
        return easyKeyValueService.get(TalkingDensity, chat.key(), 7)
    }

    companion object {
        private const val vowels = "ёэоеаяуюыи"
        private val rules = mapOf('о' to "ё", 'а' to "я", 'у' to "ю", 'ы' to "и", 'э' to "е")
        private val nonLetters = Pattern.compile(".*[^a-я]+.*")
        private val onlyDashes = Pattern.compile("^-*$")
        private val english = Pattern.compile(".*[A-Za-z]+.*")
        private val spaces = Pattern.compile("\\s+")
    }
}
