package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class SettingsExecutor(
    private val configureRepository: FunctionsConfigureRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config) {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        return {
            it.send(
                update,
                dictionary.get(Phrase.WHICH_SETTING_SHOULD_CHANGE),
                replyToUpdate = true,
                customization = customization(chat)
            )
        }
    }

    private fun customization(chat: Chat): SendMessage.() -> Unit {
        return {
            replyMarkup = FunctionId.toKeyBoard { configureRepository.isEnabled(it, chat) }
        }
    }
}
