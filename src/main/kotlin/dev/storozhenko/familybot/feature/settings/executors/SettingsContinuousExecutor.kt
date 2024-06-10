package dev.storozhenko.familybot.feature.settings.executors

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.isFromAdmin
import dev.storozhenko.familybot.common.extensions.toEmoji
import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup

@Component
class SettingsContinuousExecutor(
    private val configureRepository: FunctionsConfigureRepository,
    botConfig: BotConfig,
) : ContinuousConversationExecutor(botConfig) {
    private val log = KotlinLogging.logger {  }
    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return context.allPhrases(Phrase.WHICH_SETTING_SHOULD_CHANGE)
    }

    override suspend fun execute(context: ExecutorContext) {
        val chat = context.chat
        val callbackQuery = context.update.callbackQuery

        if (!context.client.isFromAdmin(context)) {
            log.info { "Access to settings denied" }
            context.client.execute(
                AnswerCallbackQuery(callbackQuery.id)
                    .apply {
                        showAlert = true
                        text = context.phrase(Phrase.ACCESS_DENIED)
                    },

                )
        } else {
            val function = FunctionId.entries
                .find { id -> id.desc == callbackQuery.data }

            if (function != null) {
                configureRepository.switch(function, chat)
                val isEnabled = { id: FunctionId -> configureRepository.isEnabled(id, chat) }
                context.client.execute(AnswerCallbackQuery(callbackQuery.id))
                runCatching {
                    context.client.execute(
                        EditMessageReplyMarkup().apply {
                            chatId = callbackQuery.message.chatId.toString()
                            messageId = callbackQuery.message.messageId
                            replyMarkup = FunctionId.toKeyBoard(isEnabled)
                        },
                    )
                }
                context.client.execute(
                    SendMessage(
                        chat.idString,
                        "${function.desc} → ${isEnabled.invoke(function).toEmoji()}",
                    ),
                )
            }
        }
    }
}
