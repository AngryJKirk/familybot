package space.yaroslav.familybot.executors.command.nonpublic

import kotlinx.coroutines.delay
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.sendRandomSticker
import space.yaroslav.familybot.common.extensions.startOfDay
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.CommandByUser
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.models.telegram.stickers.StickerPack
import space.yaroslav.familybot.repos.CommandHistoryRepository

abstract class SendRandomStickerExecutor(
    private val historyRepository: CommandHistoryRepository
) : CommandExecutor() {

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {

        if (isInvokedToday(context.update.toUser())) {
            return {}
        }

        return {
            it.send(context, getMessage())
            delay(1000)
            it.sendRandomSticker(context, getStickerPack())
        }
    }

    private fun isInvokedToday(user: User): Boolean {
        val commandsFromUserToday = historyRepository.get(
            user,
            from = startOfDay()
        ).map(CommandByUser::command)
        return commandsFromUserToday.any { it == command() }
    }

    abstract fun getMessage(): String
    abstract fun getStickerPack(): StickerPack
}
