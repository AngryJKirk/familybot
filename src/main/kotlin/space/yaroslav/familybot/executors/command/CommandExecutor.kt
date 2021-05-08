package space.yaroslav.familybot.executors.command

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.getCommand
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.telegram.BotConfig
import space.yaroslav.familybot.telegram.FamilyBot

abstract class CommandExecutor(private val config: BotConfig) : Executor {
    override fun canExecute(message: Message): Boolean {
        return command() == message.getCommand {
            config.botname ?: throw FamilyBot.InternalException("Bot name should be set up")
        }
    }

    override fun priority(update: Update): Priority {
        return Priority.MEDIUM
    }

    open fun isLoggable(): Boolean {
        return true
    }

    abstract fun command(): Command
}
