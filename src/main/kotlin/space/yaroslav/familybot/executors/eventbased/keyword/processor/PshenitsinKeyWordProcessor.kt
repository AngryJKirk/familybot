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

    override fun canProcess(executorContext: ExecutorContext): Boolean {
        val text = executorContext.message.text ?: return false
        return containsSymbolsY(text) && isTolerant(executorContext.message.chatId).not()
    }

    override fun process(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
            val text = talkingService
                .getReplyToUser(executorContext)
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
                executorContext,
                text,
                shouldTypeBeforeSend = true,
                replyToUpdate = true
            )

            keyValueService.put(PshenitsinTolerance, executorContext.chatKey, true, Duration.ofMinutes(1))
        }
    }

    private fun isTolerant(chatId: Long): Boolean {
        return keyValueService.get(PshenitsinTolerance, ChatEasyKey(chatId), false)
    }

    private fun containsSymbolsY(text: String): Boolean {
        return text
            .split(Regex("\\s+"))
            .any { word -> word.toCharArray().all { c -> c.lowercaseChar() == 'ы' } }
    }
}
