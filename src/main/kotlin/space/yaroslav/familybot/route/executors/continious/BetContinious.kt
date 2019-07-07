package space.yaroslav.familybot.route.executors.continious

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.CommandByUser
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.CommandHistoryRepository
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.PidorCompetitionService
import space.yaroslav.familybot.route.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.ThreadLocalRandom

@Component
class BetContinious(
    override val botConfig: BotConfig,
    private val dictionary: Dictionary,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val pidorRepository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService
) : ContiniousConversation {

    override fun getDialogMessage() = dictionary.get(Phrase.BET_INITIAL_MESSAGE)

    override fun command() = Command.BET
    override fun canExecute(message: Message): Boolean {
        return message.isReply
            && message.replyToMessage.from.userName == botConfig.botname
            && message.replyToMessage.text ?: "" == getDialogMessage()
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val now = LocalDate.now()
        val user = update.toUser()
        val chatId = update.message.chatId
        val commands = commandHistoryRepository.get(
            user, LocalDateTime.of(LocalDate.of(now.year, now.month, 1), LocalTime.MIDNIGHT)
                .toInstant(ZoneOffset.UTC)
        )
        if (isBetAlreadyWas(commands)) {
            return {
                it.send(update, dictionary.get(Phrase.BET_ALREADY_WAS))
            }
        }
        val number = extractBetNumber(update)
        if (number == null || number !in 1..3) {
            return {
                it.send(update, dictionary.get(Phrase.BET_BREAKING_THE_RULES_FIRST))
                Thread.sleep(1000)
                it.send(update, dictionary.get(Phrase.BET_BREAKING_THE_RULES_SECOND))
            }
        }
        val isItWinner = ThreadLocalRandom.current().nextBoolean()
        return {
            runBlocking {
                if (isItWinner) {
                    it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_ZATRAVOCHKA)))
                    launch { repeat(number) { pidorRepository.removePidorRecord(user) } }
                    delay(2000)
                    it.send(update, dictionary.get(Phrase.BET_WIN))
                    delay(2000)
                    it.send(update, winEndPhrase(number))
                } else {
                    it.send(update, dictionary.get(Phrase.BET_ZATRAVOCHKA))
                    launch { addPidorsMultiplyTimesWithDayShift(number, user) }
                    delay(2000)
                    it.send(update, dictionary.get(Phrase.BET_LOSE))
                    delay(2000)
                    it.send(update, explainPhrase(number))
                }
                delay(2000)
                pidorCompetitionService.pidorCompetition(update)?.invoke(it)
            }
        }
    }

    private fun addPidorsMultiplyTimesWithDayShift(number: Int, user: User) {
        var i: Int = number
        while (i != 0) {
            pidorRepository.addPidor(
                Pidor(
                    user,
                    LocalDateTime
                        .now()
                        .toLocalDate()
                        .atStartOfDay()
                        .plusDays(i.toLong())
                        .toInstant(ZoneOffset.UTC)
                )
            )
            i--
        }
    }

    private fun extractBetNumber(update: Update) =
        update.message.text.split(" ")[0].toIntOrNull()

    private fun isBetAlreadyWas(commands: List<CommandByUser>) =
        commands.any { it.command == command() }

    private fun winEndPhrase(betNumber: Int): String {
        val plur = Pluralization.getPlur(betNumber)
        val winPhraseTemplate = dictionary.get(Phrase.BET_WIN_END)
        return when (plur) {
            Pluralization.ONE -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", dictionary.get(Phrase.PLURALIZED_PIDORSKOE_ONE))
                    .replace("$2", dictionary.get(Phrase.PLURALIZED_OCHKO_ONE))
            }
            Pluralization.FEW -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", dictionary.get(Phrase.PLURALIZED_PIDORSKOE_FEW))
                    .replace("$2", dictionary.get(Phrase.PLURALIZED_OCHKO_FEW))
            }
            Pluralization.MANY -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", dictionary.get(Phrase.PLURALIZED_PIDORSKOE_MANY))
                    .replace("$2", dictionary.get(Phrase.PLURALIZED_OCHKO_MANY))
            }
        }
    }

    private fun explainPhrase(betNumber: Int): String {
        val plur = Pluralization.getPlur(betNumber)
        val explainTemplate = dictionary.get(Phrase.BET_EXPLAIN)
        return when (plur) {
            Pluralization.ONE -> {
                dictionary.get(Phrase.BET_EXPLAIN_SINGLE_DAY)
            }
            Pluralization.FEW -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", dictionary.get(Phrase.PLURALIZED_NEXT_FEW))
                    .replace("$2", dictionary.get(Phrase.PLURALIZED_DAY_FEW))
            }
            Pluralization.MANY -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", dictionary.get(Phrase.PLURALIZED_NEXT_MANY))
                    .replace("$2", dictionary.get(Phrase.PLURALIZED_DAY_MANY))
            }
        }
    }
}
