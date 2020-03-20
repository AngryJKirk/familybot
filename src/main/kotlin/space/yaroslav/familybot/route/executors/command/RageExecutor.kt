package space.yaroslav.familybot.route.executors.command

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.route.executors.Configurable
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.route.services.state.RageModeState
import space.yaroslav.familybot.route.services.state.StateService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class RageExecutor(
    private val commandHistoryRepository: CommandHistoryRepository,
    private val dictionary: Dictionary,
    private val stateService: StateService,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    private val logger = LoggerFactory.getLogger(RageExecutor::class.java)

    override fun getFunctionId(): FunctionId {
        return FunctionId.RAGE
    }

    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        if (isRageForced(update)) {
            logger.warn("Someone forced ${command()}")
            stateService.setStateForChat(chat.id, RageModeState(20, Duration.ofMinutes(10)))
            return {
                it.send(update, dictionary.get(Phrase.RAGE_INITIAL))
            }
        }

        if (isFirstLaunch(chat)) {
            return {
                it.send(update, dictionary.get(Phrase.TECHNICAL_ISSUE))
            }
        }

        if (isCooldown(update)) {
            return {
                it.send(update, dictionary.get(Phrase.RAGE_DONT_CARE_ABOUT_YOU))
            }
        }
        stateService.setStateForChat(chat.id, RageModeState(20, Duration.ofMinutes(10)))
        return {
            it.send(update, dictionary.get(Phrase.RAGE_INITIAL))
        }
    }

    private fun isCooldown(update: Update): Boolean {
        val commands = commandHistoryRepository.get(
            update.toUser(),
            from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        )
        return commands.any { it.command == command() }
    }

    private fun isFirstLaunch(chat: Chat): Boolean {
        val command = commandHistoryRepository
            .getTheFirst(chat) ?: return true

        val oneDayAgoDate = LocalDateTime
            .now()
            .plusDays(1)
                .toInstant(ZoneOffset.UTC)

        return command.date.isBefore(oneDayAgoDate)
    }

    private fun isRageForced(update: Update): Boolean {
        return update.message.text.contains("FORCED" + update.toUser().id.toString().takeLast(4))
    }
}
