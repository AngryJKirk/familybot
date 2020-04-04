package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class HelpCommandExecutor(private val dictionary: Dictionary, config: BotConfig) : CommandExecutor(config) {

    override fun command(): Command {
        return Command.HELP
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return { it.send(update, dictionary.get(Phrase.HELP_MESSAGE), enableHtml = true) }
    }
}
