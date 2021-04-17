package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Component
class BetExecutor(
    private val commandHistoryRepository: CommandHistoryRepository,
    private val dictionary: Dictionary,
    config: BotConfig
) : CommandExecutor(config), Configurable {
    private val log = getLogger()

    override fun getFunctionId() = FunctionId.PIDOR

    override fun command() = Command.BET

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val now = LocalDate.now()
        val commands = commandHistoryRepository.get(
            update.toUser(),
            LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
        if (isBetAlreadyDone(commands)) {
            log.info("Bet was done already, commands are [{}]", commands)
            return { it.send(update, context.get(Phrase.BET_ALREADY_WAS), shouldTypeBeforeSend = true) }
        }
        return {
            it.send(
                update,
                context.get(Phrase.BET_INITIAL_MESSAGE),
                replyToUpdate = true,
                shouldTypeBeforeSend = true,
                customization = { replyMarkup = ForceReplyKeyboard(true, true) }
            )
        }
    }

    override fun isLoggable() = false

    private fun isBetAlreadyDone(commands: List<CommandByUser>) =
        commands.filter { it.command == command() }.size > 1
}
