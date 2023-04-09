package dev.storozhenko.familybot.feature.help

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class HelpCommandExecutor : CommandExecutor() {

    override fun command(): Command {
        return Command.HELP
    }

    override suspend fun execute(context: ExecutorContext) {
        context.sender.send(
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
