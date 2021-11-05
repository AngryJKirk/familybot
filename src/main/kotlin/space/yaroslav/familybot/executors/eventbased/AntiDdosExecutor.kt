@file:Suppress("unused")

package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
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
    override fun getFunctionId(executorContext: ExecutorContext): FunctionId {
        return FunctionId.ANTIDDOS
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val update = executorContext.update
        val message = executorContext.phrase(Phrase.STOP_DDOS)
        return when {
            update.hasCallbackQuery() -> callbackQueryCase(executorContext, message)
            update.hasMessage() -> messageCase(executorContext, message)
            else -> { _ -> }
        }
    }

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        return if (executorContext.command != null) {
            easyKeyValueService.get(CommandLimit, executorContext.message.key(), 0) >= 5
        } else {
            false
        }
    }

    override fun priority(executorContext: ExecutorContext): Priority {
        return Priority.HIGH
    }

    private fun messageCase(
        executorContext: ExecutorContext,
        message: String
    ): suspend (AbsSender) -> Unit = { it.send(executorContext, message) }

    private fun callbackQueryCase(
        executorContext: ExecutorContext,
        message: String
    ): suspend (AbsSender) -> Unit = { it ->
        it.execute(
            AnswerCallbackQuery(executorContext.update.callbackQuery.id)
                .apply {
                    showAlert = true
                    text = message
                }

        )
    }
}
