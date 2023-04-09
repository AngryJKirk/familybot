package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.core.executors.ContiniousConversationExecutor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.telegram.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class SettingsContiniousExecutor(
    private val configureRepository: FunctionsConfigureRepository,
    botConfig: BotConfig
) : ContiniousConversationExecutor(botConfig) {
    private val log = getLogger()
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return context.allPhrases(Phrase.WHICH_SETTING_SHOULD_CHANGE)
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            val chat = context.chat
            val callbackQuery = context.update.callbackQuery

            if (!it.isFromAdmin(context)) {
                log.info("Access to settings denied")
                it.execute(
                    AnswerCallbackQuery(callbackQuery.id)
                        .apply {
                            showAlert = true
                            text = context.phrase(Phrase.ACCESS_DENIED)
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
                    runCatching {
                        it.execute(
                            EditMessageReplyMarkup().apply {
                                chatId = callbackQuery.message.chatId.toString()
                                messageId = callbackQuery.message.messageId
                                replyMarkup = FunctionId.toKeyBoard(isEnabled)
                            }
                        )
                    }
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
