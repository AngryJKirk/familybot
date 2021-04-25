@file:Suppress("unused")

package space.yaroslav.familybot.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.getCommand
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.executors.Executor
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.services.settings.CommandLimit
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AntiDdosExecutor(
    private val config: BotConfig,
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService
) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ANTIDDOS
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val message = dictionary.get(Phrase.STOP_DDOS, update)
        return when {
            update.hasCallbackQuery() -> callbackQueryCase(update, message)
            update.hasMessage() -> messageCase(update, message)
            else -> { _ -> }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return if (message.getCommand { config.botname } != null) {
            easyKeyValueService.get(CommandLimit, message.key(), 0) >= 5
        } else {
            false
        }
    }

    override fun priority(update: Update): Priority {
        return Priority.HIGH
    }

    private fun selectUser(message: Message): User {
        val user = message.from
        return if (user.userName == config.botname) {
            message.replyToMessage.from
        } else {
            user
        }
    }

    private fun messageCase(
        update: Update,
        message: String
    ): suspend (AbsSender) -> Unit = { it.send(update, message) }

    private fun callbackQueryCase(
        update: Update,
        message: String
    ): suspend (AbsSender) -> Unit = { it ->
        it.execute(
            AnswerCallbackQuery(update.callbackQuery.id)
                .apply {
                    showAlert = true
                    text = message
                }

        )
    }
}
