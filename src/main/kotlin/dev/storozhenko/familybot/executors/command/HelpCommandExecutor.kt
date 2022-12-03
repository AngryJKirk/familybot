package dev.storozhenko.familybot.executors.command

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.telegram.Command
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class HelpCommandExecutor : CommandExecutor() {

    override fun command(): Command {
        return Command.HELP
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            it.send(
                context,
                context.phrase(Phrase.HELP_MESSAGE),
                enableHtml = true,
                customization = {
                    disableWebPagePreview = true
                    disableNotification = true
                }
            )
        }
    }
}
