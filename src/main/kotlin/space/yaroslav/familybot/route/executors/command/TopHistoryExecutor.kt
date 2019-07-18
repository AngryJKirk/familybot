package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository
import space.yaroslav.familybot.route.models.Command

@Component
class TopHistoryExecutor(private val chatLogRepository: ChatLogRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.TOP_HISTORY
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val randomStory =
            chatLogRepository.getAll()
                .subList(0, 700)
                .filterNot { it.contains("http", ignoreCase = true) }
                .randomNotNull()
        return { it.send(update, randomStory) }
    }
}
