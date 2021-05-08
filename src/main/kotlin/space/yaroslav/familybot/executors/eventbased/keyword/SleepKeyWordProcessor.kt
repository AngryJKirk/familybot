package space.yaroslav.familybot.executors.eventbased.keyword

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.sendSticker
import space.yaroslav.familybot.models.telegram.stickers.Sticker

@Component
class SleepKeyWordProcessor : KeyWordProcessor {
    override fun canProcess(message: Message): Boolean {
        val text = message.text ?: return false
        return text.contains("спать") || text.contains("сплю")
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        return { it.sendSticker(update, Sticker.SWEET_DREAMS, replyToUpdate = true) }
    }

    override fun isRandom(message: Message) = true
}
