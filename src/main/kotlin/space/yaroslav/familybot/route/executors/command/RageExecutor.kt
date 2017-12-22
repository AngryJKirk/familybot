package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.toChat
import space.yaroslav.familybot.common.toUser
import space.yaroslav.familybot.repos.ifaces.CommandByUser
import space.yaroslav.familybot.repos.ifaces.HistoryRepository
import space.yaroslav.familybot.repos.ifaces.RagemodeRepository
import space.yaroslav.familybot.route.models.Command
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class RageExecutor(val historyRepository: HistoryRepository,
                   val configRepository: RagemodeRepository) : CommandExecutor() {
    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        val commands = historyRepository.get(update.toUser(),
                from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant())
        if (isCooldown(commands)) {
            return {
                it.execute(SendMessage(update.message.chatId, "Да похуй мне на тебя, чертила"))
            }
        }
        configRepository.enable(10, 20, chat)
        return {
            it.execute(SendMessage(update.message.chatId, "НУ ВЫ ОХУЕВШИЕ"))
        }
    }

    private fun isCooldown(commands: List<CommandByUser>): Boolean {
        return commands
                .map { it.command }
                .contains(command())
    }

}