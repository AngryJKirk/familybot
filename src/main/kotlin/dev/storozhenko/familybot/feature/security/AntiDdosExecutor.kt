package dev.storozhenko.familybot.feature.security

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.executors.Executor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.routers.models.Priority
import dev.storozhenko.familybot.feature.settings.models.CommandLimit
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery

@Component
class AntiDdosExecutor(
    private val easyKeyValueService: EasyKeyValueService,
) : Executor, Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.ANTIDDOS
    }

    override suspend fun execute(context: ExecutorContext) {
        val update = context.update
        val message = context.phrase(Phrase.STOP_DDOS)
        when {
            update.hasCallbackQuery() -> callbackQueryCase(context, message)
            update.hasMessage() -> messageCase(context, message)
            else -> {}
        }
    }

    override fun canExecute(context: ExecutorContext): Boolean {
        return if (context.command != null) {
            easyKeyValueService.get(CommandLimit, context.userAndChatKey, 0) >= 5
        } else {
            false
        }
    }

    override fun priority(context: ExecutorContext): Priority {
        return Priority.HIGH
    }

    private suspend fun messageCase(
        context: ExecutorContext,
        message: String,
    ) {
        context.sender.send(context, message)
    }

    private fun callbackQueryCase(
        context: ExecutorContext,
        message: String,
    ) {
        context.sender.execute(
            AnswerCallbackQuery(context.update.callbackQuery.id)
                .apply {
                    showAlert = true
                    text = message
                },

            )
    }
}
