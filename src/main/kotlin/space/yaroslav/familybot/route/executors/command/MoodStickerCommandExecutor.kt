package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendSticker
import org.telegram.telegrambots.api.methods.stickers.GetStickerSet
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class MoodStickerCommandExecutor(
    val historyRepository: CommandHistoryRepository,
    val dictionary: Dictionary
) : CommandExecutor {
    override fun command() = Command.WHATS_MOOD_TODAY

    override fun execute(update: Update): (AbsSender) -> Unit {
        val commandsFromUserToday = historyRepository.get(
            update.toUser(),
            from = LocalDate.now().atTime(0, 0).toInstant(ZoneOffset.UTC)
        ).map { it.command }
        val chatId = update.toChat().id
        if (commandsFromUserToday.any { it == command() }) {
            return {}
        }

        return {
            val sticker = it.execute(GetStickerSet("youaretoday")).stickers.random().fileId
            it.execute(SendMessage(chatId, "Какой ты сегодня?"))
            Thread.sleep(1000)
            it.sendSticker(
                SendSticker()
                    .setSticker(sticker)
                    .setChatId(chatId)
            )
        }
    }
}
