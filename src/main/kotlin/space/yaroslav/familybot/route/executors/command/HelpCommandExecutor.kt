package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
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
