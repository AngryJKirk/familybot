package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.stickers.StickerPack
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class MoodStickerCommandExecutor(
    historyRepository: CommandHistoryRepository,
    config: BotConfig
) : SendRandomStikerExecutor(config, historyRepository) {
    override fun getMessage() = "Какой ты сегодня?"

    override fun getStickerPack() = StickerPack.YOU_ARE_TODAY

    override fun command() = Command.WHATS_MOOD_TODAY
}
