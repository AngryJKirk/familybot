package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.repos.CommandHistoryRepository
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class RageExecutor(
    private val commandHistoryRepository: CommandHistoryRepository,
    private val dictionary: Dictionary,
    private val easySettingsService: EasySettingsService,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    private val log = getLogger()

    companion object {
        const val AMOUNT_OF_RAGE_MESSAGES = 20L
    }

    override fun getFunctionId(): FunctionId {
        return FunctionId.RAGE
    }

    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val context = dictionary.createContext(chat)
        if (isRageForced(update)) {
            log.warn("Someone forced ${command()}")
            easySettingsService.put(RageMode, chat.key(), AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
            return {
                it.send(update, context.get(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
            }
        }

        if (isFirstLaunch(chat)) {
            log.info("First launch of ${command()} was detected, avoiding that")
            return {
                it.send(update, context.get(Phrase.TECHNICAL_ISSUE), shouldTypeBeforeSend = true)
            }
        }

        if (isCooldown(update)) {
            log.info("There is a cooldown of ${command()}")
            return {
                it.send(update, context.get(Phrase.RAGE_DONT_CARE_ABOUT_YOU), shouldTypeBeforeSend = true)
            }
        }
        easySettingsService.put(RageMode, chat.key(), AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
        return {
            it.send(update, context.get(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
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
            .minusDays(1)
            .toInstant(ZoneOffset.UTC)

        return command.date.isAfter(oneDayAgoDate)
    }

    private fun isRageForced(update: Update): Boolean {
        return update.message.text.contains("FORCED" + update.toUser().id.toString().takeLast(4))
    }
}
