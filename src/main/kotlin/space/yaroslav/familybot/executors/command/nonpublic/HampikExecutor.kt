package space.yaroslav.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.stickers.StickerPack
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class HampikExecutor(
    historyRepository: CommandHistoryRepository,
    config: BotConfig
) : SendRandomStickerExecutor(config, historyRepository) {

    override fun getMessage() = "Какой ты сегодня Андрей?"

    override fun getStickerPack() = StickerPack.HAMPIK_PACK

    override fun command() = Command.HAMPIK
}
