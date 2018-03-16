package space.yaroslav.familybot.route.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toEmoji
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
            val callbackQuery = update.callbackQuery
            if (checkRights(it, update)) {
                val function = FunctionId
                        .values()
                        .find { it.desc == callbackQuery.data }
                if (function != null) {
                    configureRepository.switch(function, chat)
                    it.execute(AnswerCallbackQuery()
                            .setCallbackQueryId(callbackQuery.id))
                    it.execute(EditMessageReplyMarkup()
                            .setChatId(callbackQuery.message.chatId)
                            .setMessageId(callbackQuery.message.messageId)
                            .setReplyMarkup(FunctionId.toKeyBoard { configureRepository.isEnabled(it, chat) }))
                    it.execute(SendMessage(chat.id, "${function.desc} -> ${configureRepository.isEnabled(function, chat).toEmoji()}"))
                }
            } else{
                it.execute(AnswerCallbackQuery().setCallbackQueryId(callbackQuery.id)
                        .setShowAlert(true).setText("Ну ты и пидор, не для тебя ягодка росла"))
            }
        }

    }

    private fun checkRights(sender: AbsSender, update: Update): Boolean {
        val admins = sender.execute(GetChatAdministrators().setChatId(update.toChat().id))
        return admins.find { it.user.id == update.callbackQuery.from.id } != null
    }

}
