package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.parseCommand
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.executors.Executor
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.Priority
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class AntiDdosExecutor(
    private val repositoryCommand: CommandHistoryRepository,
    private val config: BotConfig,
    private val dictionary: Dictionary
) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ANTIDDOS
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val message = dictionary.get(Phrase.STOP_DDOS)
        return when {
            update.hasCallbackQuery() -> callbackQueryCase(update, message)
            update.hasMessage() -> messageCase(update, message)
            else -> { _ -> }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return repositoryCommand
            .get(selectUser(message).toUser(telegramChat = message.chat))
            .groupBy(CommandByUser::command)
            .filterValues { it.size >= 5 }
            .keys
            .contains(getText(message).parseCommand())
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

    private fun getText(message: Message): String? {
        return if (message.from.userName == config.botname) {
            message.replyToMessage?.text
        } else {
            message.text
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
            AnswerCallbackQuery()
                .setCallbackQueryId(update.callbackQuery.id)
                .setShowAlert(true)
                .setText(message)
        )
    }
}
