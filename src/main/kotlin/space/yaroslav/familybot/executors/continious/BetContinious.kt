package space.yaroslav.familybot.executors.continious

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDice
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.common.extensions.untilNextMonth
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.dictionary.Pluralization
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorCompetitionService
import space.yaroslav.familybot.services.settings.BetTolerance
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class BetContinious(
    private val dictionary: Dictionary,
    private val pidorRepository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig
) : ContiniousConversation(botConfig) {
    private val diceNumbers = listOf(1, 2, 3, 4, 5, 6)
    override fun getDialogMessage(message: Message): String {
        return dictionary.get(Phrase.BET_INITIAL_MESSAGE, message.chat.toChat().key())
    }

    override fun command() = Command.BET

    override fun canExecute(message: Message): Boolean {
        return message.isReply &&
            message.replyToMessage.from.userName == botConfig.botName &&
            message.replyToMessage.text ?: "" == getDialogMessage(message)
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val user = update.toUser()
        val chatId = update.message.chatId
        val key = update.key()

        if (isBetAlreadyDone(key)) {
            return {
                it.send(update, context.get(Phrase.BET_ALREADY_WAS), shouldTypeBeforeSend = true)
            }
        }
        val number = extractBetNumber(update)
        if (number == null || number !in 1..3) {
            return {
                it.send(update, context.get(Phrase.BET_BREAKING_THE_RULES_FIRST), shouldTypeBeforeSend = true)
                it.send(update, context.get(Phrase.BET_BREAKING_THE_RULES_SECOND), shouldTypeBeforeSend = true)
            }
        }
        val winnableNumbers = diceNumbers.shuffled().subList(0, 3)
        return {
            it.send(
                update,
                "${context.get(Phrase.BET_WINNABLE_NUMBERS_ANNOUNCEMENT)} ${formatWinnableNumbers(winnableNumbers)}",
                shouldTypeBeforeSend = true
            )
            it.send(update, context.get(Phrase.BET_ZATRAVOCHKA), shouldTypeBeforeSend = true)
            val diceMessage = it.execute(SendDice(chatId.toString()))
            delay(4000)
            val isItWinner = winnableNumbers.contains(diceMessage.dice.value)
            if (isItWinner) {
                coroutineScope { launch { repeat(number) { pidorRepository.removePidorRecord(user) } } }
                it.send(update, context.get(Phrase.BET_WIN), shouldTypeBeforeSend = true)
                it.send(update, winEndPhrase(number, context), shouldTypeBeforeSend = true)
            } else {
                coroutineScope { launch { addPidorsMultiplyTimesWithDayShift(number, user) } }
                it.send(update, context.get(Phrase.BET_LOSE), shouldTypeBeforeSend = true)
                it.send(update, explainPhrase(number, context), shouldTypeBeforeSend = true)
            }
            easyKeyValueService.put(BetTolerance, key, true, untilNextMonth())
            delay(2000)
            pidorCompetitionService.pidorCompetition(update).invoke(it)
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

    private fun isBetAlreadyDone(key: UserAndChatEasyKey) =
        easyKeyValueService.get(BetTolerance, key, false)

    private fun winEndPhrase(betNumber: Int, context: DictionaryContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val winPhraseTemplate = context.get(Phrase.BET_WIN_END)
        return when (plur) {
            Pluralization.ONE -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.get(Phrase.PLURALIZED_PIDORSKOE_ONE))
                    .replace("$2", context.get(Phrase.PLURALIZED_OCHKO_ONE))
            }
            Pluralization.FEW -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.get(Phrase.PLURALIZED_PIDORSKOE_FEW))
                    .replace("$2", context.get(Phrase.PLURALIZED_OCHKO_FEW))
            }
            Pluralization.MANY -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.get(Phrase.PLURALIZED_PIDORSKOE_MANY))
                    .replace("$2", context.get(Phrase.PLURALIZED_OCHKO_MANY))
            }
        }
    }

    private fun explainPhrase(betNumber: Int, context: DictionaryContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val explainTemplate = context.get(Phrase.BET_EXPLAIN)
        return when (plur) {
            Pluralization.ONE -> {
                context.get(Phrase.BET_EXPLAIN_SINGLE_DAY)
            }
            Pluralization.FEW -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.get(Phrase.PLURALIZED_NEXT_FEW))
                    .replace("$2", context.get(Phrase.PLURALIZED_DAY_FEW))
            }
            Pluralization.MANY -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.get(Phrase.PLURALIZED_NEXT_MANY))
                    .replace("$2", context.get(Phrase.PLURALIZED_DAY_MANY))
            }
        }
    }

    private fun formatWinnableNumbers(numbers: List<Int>): String {
        val orderedNumbers = numbers.sorted()
        return "${orderedNumbers[0]}, ${orderedNumbers[1]} и ${orderedNumbers[2]}"
    }
}
