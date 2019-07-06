package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary

@Component
class HelpCommandExecutor(val dictionary: Dictionary) : CommandExecutor {

    override fun command(): Command {
        return Command.HELP
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return { it.send(update, dictionary.get(Phrase.HELP_MESSAGE), enableHtml = true) }
    }
}
