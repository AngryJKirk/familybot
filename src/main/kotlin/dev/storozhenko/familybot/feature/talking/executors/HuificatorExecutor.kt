package dev.storozhenko.familybot.feature.talking.executors

import dev.storozhenko.familybot.common.extensions.dropLastDelimiter
import dev.storozhenko.familybot.common.extensions.randomBoolean

import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.models.TalkingDensity
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class HuificatorExecutor(private val easyKeyValueService: EasyKeyValueService) : Executor, Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.HUIFICATE
    }

    override fun priority(context: ExecutorContext) = Priority.RANDOM

    override suspend fun execute(context: ExecutorContext) {
        val text = context.message.text ?: return

        if (shouldHuificate(context)) {
            val huifyed = huify(text) ?: return
            context.send(huifyed, shouldTypeBeforeSend = true)
        }
    }

    override fun canExecute(context: ExecutorContext) = false

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

        val postfix = String(wordLowerCase.toCharArray().dropWhile { !VOWELS.contains(it) }.toCharArray())
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

    private fun getTalkingDensity(context: ExecutorContext) = easyKeyValueService.get(TalkingDensity, context.chatKey, 7)

    companion object {
        private const val VOWELS = "ёэоеаяуюыи"
        private val rules = mapOf('о' to "ё", 'а' to "я", 'у' to "ю", 'ы' to "и", 'э' to "е")
        private val nonLetters = Pattern.compile(".*[^a-я]+.*")
        private val onlyDashes = Pattern.compile("^-*$")
        private val english = Pattern.compile(".*[A-Za-z]+.*")
        private val spaces = Pattern.compile("\\s+")
    }
}
