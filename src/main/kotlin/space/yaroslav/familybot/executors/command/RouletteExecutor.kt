package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

const val ROULETTE_MESSAGE = "Выбери число от 1 до 6"

@Component
@Deprecated(message = "Replaced with BetExecutor")
class RouletteExecutor(
    private val commandHistoryRepository: CommandHistoryRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.ROULETTE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val now = LocalDate.now()
        val commands = commandHistoryRepository.get(
            update.toUser(),
            LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
        val chatId = update.message.chatId.toString()
        if (commands.filter { it.command == command() }.size > 1) {
            return {
                it.execute(SendMessage(chatId, dictionary.get(Phrase.ROULETTE_ALREADY_WAS)))
                delay(2000)
                it.execute(SendMessage(chatId, dictionary.get(Phrase.PIDOR)))
            }
        }
        return {
            it.execute(
                SendMessage(chatId, dictionary.get(Phrase.ROULETTE_MESSAGE))
                    .apply {
                        replyMarkup = ForceReplyKeyboard().apply { selective = true }
                        replyToMessageId = update.message.messageId
                    }
            )
        }
    }

    override fun isLoggable(): Boolean {
        return false
    }
}
