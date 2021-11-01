package space.yaroslav.familybot.executors.eventbased.keyword.processor

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.executors.eventbased.keyword.KeyWordProcessor
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

    override fun canProcess(message: Message): Boolean {
        val text = message.text ?: return false
        return containsSymbolsY(text) && isTolerant(message.chatId).not()
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val text = talkingService
                .getReplyToUser(update)
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
                update,
                text,
                shouldTypeBeforeSend = true,
                replyToUpdate = true
            )

            keyValueService.put(PshenitsinTolerance, update.toChat().key(), true, Duration.ofMinutes(1))
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
