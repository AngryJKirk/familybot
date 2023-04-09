package dev.storozhenko.familybot.feature.help

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
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
