package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId


const val SETTINGS_MESSAGE = "Какую настройку переключить?"

@Component
class SettingsExecutor(private val configureRepository: FunctionsConfigureRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        return {
            if (checkRights(it, update)) {
                val keyboard = KeyboardRow()
                FunctionId.values().forEach { id -> keyboard.add(id.desc) }
                it.execute(SendMessage(chat.id, SETTINGS_MESSAGE)
                        .setReplyToMessageId(update.message.messageId)
                        .setReplyMarkup(ReplyKeyboardMarkup()
                        .setKeyboard(listOf(keyboard))
                        .setSelective(true)
                        .setOneTimeKeyboard(true)
                        .setResizeKeyboard(true)))
            }
            else {
                it.execute(SendMessage(chat.id, "Наебать меня решил, да? " +
                        "Папку позови, только с ним на эту тему говорить буду"))
            }
        }
    }

    private fun checkRights(sender: AbsSender, update: Update): Boolean {
        val admins = sender.execute(GetChatAdministrators().setChatId(update.toChat().id))
        return admins.find { it.user.id == update.message.from.id } != null
    }

}