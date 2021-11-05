package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.dropLastDelimiter
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.randomBoolean
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.TalkingDensity
import java.util.regex.Pattern

@Component
class HuificatorExecutor(private val easyKeyValueService: EasyKeyValueService) : Executor, Configurable {
    override fun getFunctionId(executorContext: ExecutorContext): FunctionId {
        return FunctionId.HUIFICATE
    }

    override fun priority(executorContext: ExecutorContext): Priority {
        return Priority.RANDOM
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        val message = executorContext.message
        val text = message.text ?: return {}

        if (shouldHuificate(executorContext.chat)) {
            val huifyed = huify(text) ?: return { }
            return { it -> it.send(executorContext, huifyed, shouldTypeBeforeSend = true) }
        } else {
            return { }
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return false
    }

    fun huify(word: String): String? {
        val wordLowerCase = getLastWord(word).lowercase()

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
        return if (postfix.isEmpty()) {
            "хуе" + wordLowerCase.drop(2)
        } else if (rules.containsKey(postfix[0])) {
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
            randomBoolean(density)
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
