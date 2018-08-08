package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

const val ROULETTE_MESSAGE = "Выбери число от 1 до 6"

@Component
class RouletteExecutor(private val commandHistoryRepository: CommandHistoryRepository) : CommandExecutor, Configurable {

    override fun getFunctionId(): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.ROULETTE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val commands = commandHistoryRepository.get(
            update.toUser(), LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
        if (commands.filter { it.command == command() }.size > 1) {
            return {
                it.execute(SendMessage(update.message.chatId, "Ты уже крутил рулетку."))
                Thread.sleep(2000)
                it.execute(SendMessage(update.message.chatId, "Пидор."))
            }
        }
        return {
            it.execute(
                SendMessage(update.message.chatId, ROULETTE_MESSAGE)
                    .setReplyMarkup(ForceReplyKeyboard().setSelective(true))
                    .setReplyToMessageId(update.message.messageId)
            )
        }
    }

    override fun isLoggable(): Boolean {
        return false
    }
}