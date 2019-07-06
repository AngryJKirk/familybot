package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary

@Component
class SettingsExecutor(
    private val configureRepository: FunctionsConfigureRepository,
    val dictionary: Dictionary
) : CommandExecutor {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
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

    private fun customization(chat: Chat) =
        { message: SendMessage ->
            message.setReplyMarkup(FunctionId.toKeyBoard {
                configureRepository.isEnabled(
                    it,
                    chat
                )
            })
        }
}
