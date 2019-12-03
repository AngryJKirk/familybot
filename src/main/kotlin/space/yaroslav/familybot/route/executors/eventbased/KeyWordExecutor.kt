package space.yaroslav.familybot.route.executors.eventbased

import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.sendSticker
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.models.stickers.Sticker

// TODO Make more universal with actions from database
@Component
class KeyWordExecutor : Executor {
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            if (ThreadLocalRandom.current().nextInt(0, 15) == 0) {
                it.sendSticker(update, Sticker.SWEET_DREAMS, replyToUpdate = true)
            }
        }
    }

    override fun canExecute(message: Message): Boolean {
        val text = message.text
        return if (text != null) {
            text.contains("спать") || text.contains("сплю")
        } else false
    }

    override fun priority(update: Update) = Priority.LOW
}
