package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.services.talking.TalkingService

@Component
class PshenitsinKeyWordProcessor(
    private val talkingService: TalkingService
) : KeyWordProcessor {

    override fun canProcess(message: Message): Boolean {
        val text = message.text ?: return false
        return text
            .split(Regex("\\s+"))
            .any { word -> word.toCharArray().all { c -> c.lowercaseChar() == 'ы' } }
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val text = talkingService
                .getReplyToUser(update)
                .toCharArray()
                .map { ch ->
                    if (ch.isLetter()) {
                        if (ch.isUpperCase()) {
                            'Ы'
                        } else {
                            'ы'
                        }
                    } else {
                        ch
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
        }
    }
}