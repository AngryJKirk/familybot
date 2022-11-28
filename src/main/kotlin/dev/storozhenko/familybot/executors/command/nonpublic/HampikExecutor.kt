package dev.storozhenko.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import dev.storozhenko.familybot.models.telegram.Command
import dev.storozhenko.familybot.models.telegram.stickers.StickerPack
import dev.storozhenko.familybot.repos.CommandHistoryRepository

@Component
class HampikExecutor(
    historyRepository: CommandHistoryRepository
) : SendRandomStickerExecutor(historyRepository) {

    override fun getMessage() = "Какой ты сегодня Андрей?"

    override fun getStickerPack() = StickerPack.HAMPIK_PACK

    override fun command() = Command.HAMPIK
}
