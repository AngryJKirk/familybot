package space.yaroslav.familybot.route.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toEmoji
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class SettingsContinious(
    private val configureRepository: FunctionsConfigureRepository,
    override val botConfig: BotConfig,
    val dictionary: Dictionary
) : ContiniousConversation {

    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessage(): String {
        return dictionary.get(Phrase.WHICH_SETTING_SHOULD_CHANGE)
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val chat = update.toChat()
            val callbackQuery = update.callbackQuery

            if (!checkRights(it, update)) {
                it.execute(
                    AnswerCallbackQuery().setCallbackQueryId(callbackQuery.id)
                        .setShowAlert(true).setText(dictionary.get(Phrase.ACCESS_DENIED))
                )
            } else {
                val function = FunctionId
                    .values()
                    .find { id -> id.desc == callbackQuery.data }

                if (function != null) {
                    configureRepository.switch(function, chat)
                    val isEnabled = { id: FunctionId -> configureRepository.isEnabled(id, chat) }
                    it.execute(
                        AnswerCallbackQuery()
                            .setCallbackQueryId(callbackQuery.id)
                    )
                    it.execute(
                        EditMessageReplyMarkup()
                            .setChatId(callbackQuery.message.chatId)
                            .setMessageId(callbackQuery.message.messageId)
                            .setReplyMarkup(FunctionId.toKeyBoard(isEnabled))
                    )
                    it.execute(SendMessage(chat.id, "${function.desc} -> ${isEnabled.invoke(function).toEmoji()}"))
                }
            }
        }
    }

    private fun checkRights(sender: AbsSender, update: Update): Boolean {
        return sender
            .execute(GetChatAdministrators().setChatId(update.toChat().id))
            .find { it.user.id == update.callbackQuery.from.id } != null
    }
}
