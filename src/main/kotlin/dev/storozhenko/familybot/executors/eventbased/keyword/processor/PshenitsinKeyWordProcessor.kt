package dev.storozhenko.familybot.executors.eventbased.keyword.processor

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.executors.eventbased.keyword.KeyWordProcessor
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.settings.PshenitsinTolerance
import dev.storozhenko.familybot.services.talking.TalkingService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Duration

@Component
class PshenitsinKeyWordProcessor(
    private val talkingService: TalkingService,
    private val keyValueService: EasyKeyValueService
) : KeyWordProcessor {

    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return containsSymbolsY(text) && isTolerant(context.message.chatId).not()
    }

    override fun process(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
            val text = talkingService
                .getReplyToUser(context)
                .toCharArray()
                .map { ch ->
                    when {
                        ch.isLetter() && ch.isUpperCase() -> 'Ы'
                        ch.isLetter() && ch.isLowerCase() -> 'ы'
                        else -> ch
                    }
                }
                .toCharArray()
                .let(::String)

            sender.send(
                context,
                text,
                shouldTypeBeforeSend = true,
                replyToUpdate = true
            )

            keyValueService.put(PshenitsinTolerance, context.chatKey, true, Duration.ofMinutes(1))
        }
    }

    private fun isTolerant(chatId: Long): Boolean {
        return keyValueService.get(PshenitsinTolerance, ChatEasyKey(chatId), false)
    }

    private fun containsSymbolsY(text: String): Boolean {
        val splitText = text.split(Regex("\\s+"))
        return if (splitText.first().toCharArray().isEmpty()) {
            false
        } else {
            splitText.any { word -> word.toCharArray().all { c -> c.lowercaseChar() == 'ы' } }
        }
    }
}
