package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.talking.Dictionary
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
                dictionary.get(Phrase.WHICH_SETTING_SHOULD_CHANGE, update),
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
