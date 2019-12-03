package space.yaroslav.familybot.route.executors.command

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.telegram.BotConfig

abstract class CommandExecutor(private val config: BotConfig) : Executor {
    override fun canExecute(message: Message): Boolean {
        val entities = message.entities ?: return false
        return entities.any {
            it.type == "bot_command" &&
                it.text.startsWith(command().command) &&
                it.offset == 0 &&
                isAddressedToThisBot(message.text ?: "")
        }
    }

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    open fun isLoggable(): Boolean {
        return true
    }

    abstract fun command(): Command

    private fun isAddressedToThisBot(text: String): Boolean {
        if (text.contains("@").not()) {
            return true
        }
        return text.contains(command().command + "@${config.botname}")
    }
}
