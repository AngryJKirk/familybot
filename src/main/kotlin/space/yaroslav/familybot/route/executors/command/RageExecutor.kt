package space.yaroslav.familybot.route.executors.command

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.RagemodeRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class RageExecutor(
    val commandHistoryRepository: CommandHistoryRepository,
    val configRepository: RagemodeRepository,
    val dictionary: Dictionary
) : CommandExecutor, Configurable {

    private val logger = LoggerFactory.getLogger(RageExecutor::class.java)

    override fun getFunctionId(): FunctionId {
        return FunctionId.RAGE
    }

    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val chat = update.toChat()
        if (isRageForced(update)) {
            logger.warn("Someone forced ${command()}")
            configRepository.enable(10, 20, chat)
            return {
                it.execute(SendMessage(update.message.chatId, dictionary.get(Phrase.RAGE_INITIAL)))
            }
        }

        if (isFirstLaunch(chat)) {
            return {
                it.execute(SendMessage(update.message.chatId, dictionary.get(Phrase.TECHNICAL_ISSUE)))
            }
        }

        if (isCooldown(update)) {
            return {
                it.execute(SendMessage(update.message.chatId, dictionary.get(Phrase.RAGE_DONT_CARE_ABOUT_YOU)))
            }
        }

        configRepository.enable(10, 20, chat)
        return {
            it.execute(SendMessage(update.message.chatId, dictionary.get(Phrase.RAGE_INITIAL)))
        }
    }

    private fun isCooldown(update: Update): Boolean {
        val commands = commandHistoryRepository.get(
            update.toUser(),
            from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        )
        return commands
            .find { it.command == command() } != null
    }

    private fun isFirstLaunch(chat: Chat): Boolean {
        return commandHistoryRepository
            .getAll(chat)
            .sortedBy { it.date }
            .firstOrNull()
            ?.date
            ?.isToday()
            ?: true
    }

    private fun isRageForced(update: Update): Boolean {
        return update.message.text.contains("FORCED" + update.toUser().id.toString().takeLast(4))
    }
}
