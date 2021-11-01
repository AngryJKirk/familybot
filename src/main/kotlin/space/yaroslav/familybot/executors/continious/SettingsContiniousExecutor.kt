package space.yaroslav.familybot.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.isFromAdmin
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toEmoji
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class SettingsContiniousExecutor(
    private val configureRepository: FunctionsConfigureRepository,
    private val dictionary: Dictionary,
    private val botConfig: BotConfig
) : ContiniousConversationExecutor(botConfig) {
    private val log = getLogger()
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessage(message: Message): String {
        return dictionary.get(Phrase.WHICH_SETTING_SHOULD_CHANGE, message.chat.toChat().key())
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        return {
            val chat = update.toChat()
            val callbackQuery = update.callbackQuery

            if (!it.isFromAdmin(update, botConfig)) {
                log.info("Access to settings denied")
                it.execute(
                    AnswerCallbackQuery(callbackQuery.id)
                        .apply {
                            showAlert = true
                            text = dictionary.get(Phrase.ACCESS_DENIED, update)
                        }

                )
            } else {
                val function = FunctionId
                    .values()
                    .find { id -> id.desc == callbackQuery.data }

                if (function != null) {
                    configureRepository.switch(function, chat)
                    val isEnabled = { id: FunctionId -> configureRepository.isEnabled(id, chat) }
                    it.execute(AnswerCallbackQuery(callbackQuery.id))
                    it.execute(
                        EditMessageReplyMarkup().apply {
                            chatId = callbackQuery.message.chatId.toString()
                            messageId = callbackQuery.message.messageId
                            replyMarkup = FunctionId.toKeyBoard(isEnabled)
                        }
                    )
                    it.execute(
                        SendMessage(
                            chat.idString,
                            "${function.desc} → ${isEnabled.invoke(function).toEmoji()}"
                        )
                    )
                }
            }
        }
    }
}
