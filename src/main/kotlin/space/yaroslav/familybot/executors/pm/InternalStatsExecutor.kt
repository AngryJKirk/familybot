package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class InternalStatsExecutor(
    private val botConfig: BotConfig,
    private val commonRepository: CommonRepository
) : PrivateMessageExecutor {

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return {
            it.send(update, "Chats=$chats")
        }
    }

    override fun canExecute(message: Message): Boolean {
        return botConfig.developer == message.from.userName
    }

    override fun priority(update: Update): Priority {
        return Priority.HIGH
    }
}