package space.yaroslav.familybot.route.executors.continious

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Pidor
import space.yaroslav.familybot.common.Pluralization
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
        if (commands.any { it.command == command() }) {
            return {
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_ALREADY_WAS)))
            }
        }
        val number = update.message.text.split(" ")[0].toIntOrNull()
        if (number == null || number !in 1..3) {
            return {
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_BREAKING_THE_RULES_FIRST)))
                Thread.sleep(1000)
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_BREAKING_THE_RULES_SECOND)))
            }
        }
        val result = ThreadLocalRandom.current().nextBoolean()
        return {
            if (result) {
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_ZATRAVOCHKA)))
                GlobalScope.launch { repeat(number) { pidorRepository.removePidorRecord(user) } }
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_WIN)))
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, winEndPhrase(number)))
            } else {
                it.execute(SendMessage(chatId, dictionary.get(Phrase.BET_ZATRAVOCHKA)))
                GlobalScope.launch {
                    var i: Int = number
                    while (i != 0) {
                        pidorRepository.addPidor(
                            Pidor(
                                user,
                                LocalDateTime
                                    .now()
                                    .toLocalDate()
                                    .atStartOfDay()
                                    .plusDays(number.toLong())
                                    .toInstant(ZoneOffset.UTC)
                            )
                        )
                        i--
                    }
                }
                Thread.sleep(2000)
                it.execute(
                    SendMessage(
                        chatId,
                        dictionary.get(Phrase.BET_LOSE)
                    )
                )
                Thread.sleep(2000)
                it.execute(SendMessage(chatId, explainPhrase(number)))
            }
            Thread.sleep(2000)
            pidorCompetitionService.pidorCompetition(update)?.invoke(it)
        }
    }

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
