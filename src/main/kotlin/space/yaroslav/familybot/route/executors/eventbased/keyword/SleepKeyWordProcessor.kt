package space.yaroslav.familybot.route.executors.eventbased.keyword

import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.sendSticker
import space.yaroslav.familybot.route.models.stickers.Sticker

@Component
class SleepKeyWordProcessor : KeyWordProcessor {
    override fun canProcess(message: Message): Boolean {
        val text = message.text ?: return false
        return text.contains("спать") || text.contains("сплю")
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        return {
            if (ThreadLocalRandom.current().nextInt(0, 15) == 0) {
                it.sendSticker(update, Sticker.SWEET_DREAMS, replyToUpdate = true)
            }
        }
    }
}
