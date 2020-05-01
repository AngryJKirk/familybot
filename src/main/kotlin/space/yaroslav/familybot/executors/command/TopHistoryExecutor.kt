package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class TopHistoryExecutor(private val chatLogRepository: ChatLogRepository, config: BotConfig) :
    CommandExecutor(config) {
    override fun command(): Command {
        return Command.TOP_HISTORY
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val randomStory =
            chatLogRepository.getAll()
                .subList(0, 700)
                .filterNot { it.contains("http", ignoreCase = true) }
                .random()
        return { it.send(update, randomStory) }
    }
}
