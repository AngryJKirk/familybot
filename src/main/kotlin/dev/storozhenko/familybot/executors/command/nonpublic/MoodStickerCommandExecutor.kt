package dev.storozhenko.familybot.executors.command.nonpublic

import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.repos.CommandHistoryRepository
import org.springframework.stereotype.Component

@Component
class MoodStickerCommandExecutor(
    historyRepository: CommandHistoryRepository
) : SendRandomStickerExecutor(historyRepository) {
    override fun getMessage() = "Какой ты сегодня?"

    override fun getStickerPack() = StickerPack.YOU_ARE_TODAY

    override fun command() = Command.WHATS_MOOD_TODAY
}
