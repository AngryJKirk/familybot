package space.yaroslav.familybot.executors.command.settings

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.FunctionsConfigureRepository

@Component
class SettingsExecutor(
    private val configureRepository: FunctionsConfigureRepository
) : CommandExecutor() {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            it.send(
                executorContext,
                executorContext.phrase(Phrase.WHICH_SETTING_SHOULD_CHANGE),
                replyToUpdate = true,
                customization = customization(executorContext.chat)
            )
        }
    }

    private fun customization(chat: Chat): SendMessage.() -> Unit {
        return {
            replyMarkup = FunctionId.toKeyBoard { configureRepository.isEnabled(it, chat) }
        }
    }
}
