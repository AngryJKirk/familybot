package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toKeyBoard
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId


const val SETTINGS_MESSAGE = "Какую настройку переключить?"

@Component
class SettingsExecutor(private val configureRepository: FunctionsConfigureRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        return {

                val keyboards = FunctionId.values()
                        .toList()
                        .map { it.desc }
                        .toKeyBoard()
                it.execute(SendMessage(chat.id, SETTINGS_MESSAGE)
                        .setReplyToMessageId(update.message.messageId)
                        .setReplyMarkup(ReplyKeyboardMarkup()
                                .setKeyboard(keyboards)
                                .setSelective(true)
                                .setOneTimeKeyboard(true)
                                .setResizeKeyboard(true)))
        }
    }

}