package space.yaroslav.familybot.route.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.executors.command.SETTINGS_MESSAGE
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.telegram.BotConfig

@Component
class SettingsContinious(private val configureRepository: FunctionsConfigureRepository,
                         override val botConfig: BotConfig) : ContiniousConversation {


    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessage(): String {
        return SETTINGS_MESSAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        return {
            val chat = update.toChat()
            if (checkRights(it, update)) {
                val function = FunctionId
                        .values()
                        .find { it.desc == update.message.text }
                if (function == null) {
                    it.execute(SendMessage(update.message.chatId, "Опять этот пидор какую-то хуйню написал"))
                } else {
                    configureRepository.switch(function, chat)
                    it.execute(SendMessage(chat.id,
                            "Ок, новый статус: ${currentState(function, chat)}")
                            .setReplyMarkup(ReplyKeyboardRemove()))
                }
            } else {
                it.execute(SendMessage(chat.id, "Наебать меня решил, да? " +
                        "Папку позови, только с ним на эту тему говорить буду"))
            }
        }


    }

    private fun currentState(functionId: FunctionId, chat: Chat): String {
        return when (configureRepository.isEnabled(functionId, chat)) {
            true -> "ВКЛ"
            false -> "ВЫКЛ"
        }
    }


    private fun checkRights(sender: AbsSender, update: Update): Boolean {
        val admins = sender.execute(GetChatAdministrators().setChatId(update.toChat().id))
        return admins.find { it.user.id == update.message.from.id } != null
    }

}
