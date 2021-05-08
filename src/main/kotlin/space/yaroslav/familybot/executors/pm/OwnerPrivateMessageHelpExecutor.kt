package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.telegram.BotConfig

@Component
class OwnerPrivateMessageHelpExecutor(
    onlyBotOwnerExecutors: List<OnlyBotOwnerExecutor>,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    override fun getMessagePrefix() = "help"
    private val helpMessage = onlyBotOwnerExecutors
        .map { executor -> executor.getMessagePrefix() to executor::class.java.simpleName }
        .sortedBy { (prefix, _) -> prefix }
        .joinToString("\n") { (prefix, executorName) -> "$prefix — $executorName" }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { sender -> sender.send(update, helpMessage) }
    }
}
