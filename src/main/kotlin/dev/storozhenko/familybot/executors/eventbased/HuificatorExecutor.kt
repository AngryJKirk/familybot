package dev.storozhenko.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.dropLastDelimiter
import dev.storozhenko.familybot.common.extensions.randomBoolean
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.Executor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.router.Priority
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.TalkingDensity
import java.util.regex.Pattern

@Component
class HuificatorExecutor(private val easyKeyValueService: EasyKeyValueService) : Executor, Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.HUIFICATE
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.RANDOM
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val text = context.message.text ?: return {}

        if (shouldHuificate(context)) {
            val huifyed = huify(text) ?: return { }
            return { it -> it.send(context, huifyed, shouldTypeBeforeSend = true) }
        } else {
            return { }
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
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

        if (wordLowerCase.startsWith("ху", true)) {
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

    private fun shouldHuificate(context: ExecutorContext): Boolean {
        val density = getTalkingDensity(context)
        return if (density == 0L) {
            true
        } else {
            randomBoolean(density)
        }
    }

    private fun getTalkingDensity(context: ExecutorContext): Long {
        return easyKeyValueService.get(TalkingDensity, context.chatKey, 7)
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
