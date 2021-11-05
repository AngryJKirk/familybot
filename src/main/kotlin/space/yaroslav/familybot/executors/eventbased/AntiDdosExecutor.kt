@file:Suppress("unused")

package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.router.Priority
import space.yaroslav.familybot.services.settings.CommandLimit
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AntiDdosExecutor(
    private val config: BotConfig,
    private val easyKeyValueService: EasyKeyValueService
) : Executor, Configurable {
    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.ANTIDDOS
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val update = context.update
        val message = context.phrase(Phrase.STOP_DDOS)
        return when {
            update.hasCallbackQuery() -> callbackQueryCase(context, message)
            update.hasMessage() -> messageCase(context, message)
            else -> { _ -> }
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

    private fun messageCase(
        context: ExecutorContext,
        message: String
    ): suspend (AbsSender) -> Unit = { it.send(context, message) }

    private fun callbackQueryCase(
        context: ExecutorContext,
        message: String
    ): suspend (AbsSender) -> Unit = { it ->
        it.execute(
            AnswerCallbackQuery(context.update.callbackQuery.id)
                .apply {
                    showAlert = true
                    text = message
                }

        )
    }
}
