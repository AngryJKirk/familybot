package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command

@Component
class HelpCommandExecutor : CommandExecutor() {

    override fun command(): Command {
        return Command.HELP
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            it.send(
                executorContext,
                executorContext.phrase(Phrase.HELP_MESSAGE),
                enableHtml = true,
                customization = {
                    disableWebPagePreview = true
                    disableNotification = true
                }
            )
        }
    }
}
