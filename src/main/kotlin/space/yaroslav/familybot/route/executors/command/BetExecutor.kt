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
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Component
class BetExecutor(
    private val commandHistoryRepository: CommandHistoryRepository,
    private val dictionary: Dictionary
) : CommandExecutor, Configurable {
    override fun getFunctionId() = FunctionId.PIDOR

    override fun command() = Command.BET

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val commands = commandHistoryRepository.get(
            update.toUser(), LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
        if (commands.filter { it.command == command() }.size > 1) {
            return {
                it.execute(SendMessage(update.message.chatId, dictionary.get(Phrase.BET_ALREADY_WAS)))
            }
        }
        return {
            it.execute(
                SendMessage(update.message.chatId, dictionary.get(Phrase.BET_INITIAL_MESSAGE))
                    .setReplyMarkup(ForceReplyKeyboard().setSelective(true))
                    .setReplyToMessageId(update.message.messageId)
            )
        }
    }

    override fun isLoggable() = false
}
