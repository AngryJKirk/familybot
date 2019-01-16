package space.yaroslav.familybot.route.executors.eventbased

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.parseCommand
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
    val repositoryCommand: CommandHistoryRepository,
    val config: BotConfig,
    val dictionary: Dictionary
) : Executor, Configurable {
    override fun getFunctionId(): FunctionId {
        return FunctionId.ANTIDDOS
    }


    override fun execute(update: Update): (AbsSender) -> Unit {
        val message = dictionary.get(Phrase.STOP_DDOS)
        return when {
            update.hasCallbackQuery() -> { it ->
                it.execute(
                    AnswerCallbackQuery()
                        .setCallbackQueryId(update.callbackQuery.id)
                        .setShowAlert(true)
                        .setText(message)
                )
            }
            update.hasMessage() -> { it -> it.execute(SendMessage(update.message.chatId, message)) }
            else -> { _ -> }
        }
    }

    override fun canExecute(message: Message): Boolean {
        return repositoryCommand
            .get(selectUser(message).toUser(telegramChat = message.chat))
            .groupBy { it.command }
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
}
