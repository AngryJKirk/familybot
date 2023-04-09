package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class SettingsExecutor(
    private val configureRepository: FunctionsConfigureRepository
) : CommandExecutor() {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            it.send(
                context,
                context.phrase(Phrase.WHICH_SETTING_SHOULD_CHANGE),
                replyToUpdate = true,
                customization = customization(context.chat)
            )
        }
    }

    private fun customization(chat: Chat): SendMessage.() -> Unit {
        return {
            replyMarkup = FunctionId.toKeyBoard { configureRepository.isEnabled(it, chat) }
        }
    }
}
