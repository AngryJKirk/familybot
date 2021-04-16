package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.stickers.StickerPack
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
