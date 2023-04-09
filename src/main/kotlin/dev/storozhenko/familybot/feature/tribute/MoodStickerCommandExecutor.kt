package dev.storozhenko.familybot.feature.tribute

import dev.storozhenko.familybot.core.executors.SendRandomStickerExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import org.springframework.stereotype.Component

@Component
class MoodStickerCommandExecutor(
    historyRepository: CommandHistoryRepository
) : SendRandomStickerExecutor(historyRepository) {
    override fun getMessage() = "Какой ты сегодня?"

    override fun getStickerPack() = StickerPack.YOU_ARE_TODAY

    override fun command() = Command.WHATS_MOOD_TODAY
}
