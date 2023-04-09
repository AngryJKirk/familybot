package dev.storozhenko.familybot.feature.tribute.executors

import dev.storozhenko.familybot.core.executors.SendRandomStickerExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.feature.logging.repos.CommandHistoryRepository
import org.springframework.stereotype.Component

@Component
class HampikExecutor(
    historyRepository: CommandHistoryRepository
) : SendRandomStickerExecutor(historyRepository) {

    override fun getMessage() = "Какой ты сегодня Андрей?"

    override fun getStickerPack() = StickerPack.HAMPIK_PACK

    override fun command() = Command.HAMPIK
}
