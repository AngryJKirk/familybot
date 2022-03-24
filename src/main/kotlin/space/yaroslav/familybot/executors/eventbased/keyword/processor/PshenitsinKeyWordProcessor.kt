package space.yaroslav.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordProcessor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.services.settings.ChatEasyKey
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.PshenitsinTolerance
import space.yaroslav.familybot.services.talking.TalkingService
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

        if (splitText.first().toCharArray().isEmpty())
        {
            return false
        }
        
        return splitText.any { word -> word.toCharArray().all { c -> c.lowercaseChar() == 'ы' } }
    }
}
