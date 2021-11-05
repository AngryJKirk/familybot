package space.yaroslav.familybot.executors.continious

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDice
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.untilNextMonth
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.dictionary.Pluralization
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.models.telegram.Pidor
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.pidor.PidorCompetitionService
import space.yaroslav.familybot.services.settings.BetTolerance
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.telegram.BotConfig
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class BetContiniousExecutor(
    private val pidorRepository: CommonRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig
) : ContiniousConversationExecutor(botConfig) {
    private val diceNumbers = listOf(1, 2, 3, 4, 5, 6)

    override fun getDialogMessage(executorContext: ExecutorContext): String {
        return executorContext.phrase(Phrase.BET_INITIAL_MESSAGE)
    }

    override fun command() = Command.BET

    override fun canExecute(executorContext: ExecutorContext): Boolean {
        val message = executorContext.message
        return message.isReply &&
            message.replyToMessage.from.userName == botConfig.botName &&
            (message.replyToMessage.text ?: "") == getDialogMessage(executorContext)
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        val user = executorContext.user
        val chatId = executorContext.message.chatId
        val key = executorContext.update.key()

        if (isBetAlreadyDone(key)) {
            return {
                it.send(executorContext, executorContext.phrase(Phrase.BET_ALREADY_WAS), shouldTypeBeforeSend = true)
            }
        }
        val number = extractBetNumber(executorContext)
        if (number == null || number !in 1..3) {
            return {
                it.send(
                    executorContext,
                    executorContext.phrase(Phrase.BET_BREAKING_THE_RULES_FIRST),
                    shouldTypeBeforeSend = true
                )
                it.send(
                    executorContext,
                    executorContext.phrase(Phrase.BET_BREAKING_THE_RULES_SECOND),
                    shouldTypeBeforeSend = true
                )
            }
        }
        val winnableNumbers = diceNumbers.shuffled().subList(0, 3)
        return {
            it.send(
                executorContext,
                "${executorContext.phrase(Phrase.BET_WINNABLE_NUMBERS_ANNOUNCEMENT)} ${
                    formatWinnableNumbers(
                        winnableNumbers
                    )
                }",
                shouldTypeBeforeSend = true
            )
            it.send(executorContext, executorContext.phrase(Phrase.BET_ZATRAVOCHKA), shouldTypeBeforeSend = true)
            val diceMessage = it.execute(SendDice(chatId.toString()))
            delay(4000)
            val isItWinner = winnableNumbers.contains(diceMessage.dice.value)
            if (isItWinner) {
                coroutineScope { launch { repeat(number) { pidorRepository.removePidorRecord(user) } } }
                it.send(executorContext, executorContext.phrase(Phrase.BET_WIN), shouldTypeBeforeSend = true)
                it.send(executorContext, winEndPhrase(number, executorContext), shouldTypeBeforeSend = true)
            } else {
                coroutineScope { launch { addPidorsMultiplyTimesWithDayShift(number, user) } }
                it.send(executorContext, executorContext.phrase(Phrase.BET_LOSE), shouldTypeBeforeSend = true)
                it.send(executorContext, explainPhrase(number, executorContext), shouldTypeBeforeSend = true)
            }
            easyKeyValueService.put(BetTolerance, key, true, untilNextMonth())
            delay(2000)
            pidorCompetitionService.pidorCompetition(executorContext).invoke(it)
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

    private fun extractBetNumber(executorContext: ExecutorContext) =
        executorContext.message.text.split(" ")[0].toIntOrNull()

    private fun isBetAlreadyDone(key: UserAndChatEasyKey) =
        easyKeyValueService.get(BetTolerance, key, false)

    private fun winEndPhrase(betNumber: Int, executorContext: ExecutorContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val winPhraseTemplate = executorContext.phrase(Phrase.BET_WIN_END)
        return when (plur) {
            Pluralization.ONE -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", executorContext.phrase(Phrase.PLURALIZED_PIDORSKOE_ONE))
                    .replace("$2", executorContext.phrase(Phrase.PLURALIZED_OCHKO_ONE))
            }
            Pluralization.FEW -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", executorContext.phrase(Phrase.PLURALIZED_PIDORSKOE_FEW))
                    .replace("$2", executorContext.phrase(Phrase.PLURALIZED_OCHKO_FEW))
            }
            Pluralization.MANY -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", executorContext.phrase(Phrase.PLURALIZED_PIDORSKOE_MANY))
                    .replace("$2", executorContext.phrase(Phrase.PLURALIZED_OCHKO_MANY))
            }
        }
    }

    private fun explainPhrase(betNumber: Int, executorContext: ExecutorContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val explainTemplate = executorContext.phrase(Phrase.BET_EXPLAIN)
        return when (plur) {
            Pluralization.ONE -> {
                executorContext.phrase(Phrase.BET_EXPLAIN_SINGLE_DAY)
            }
            else -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", executorContext.phrase(Phrase.PLURALIZED_NEXT_MANY))
                    .replace("$2", executorContext.phrase(Phrase.PLURALIZED_DAY_MANY))
            }
        }
    }

    private fun formatWinnableNumbers(numbers: List<Int>): String {
        val orderedNumbers = numbers.sorted()
        return "${orderedNumbers[0]}, ${orderedNumbers[1]} и ${orderedNumbers[2]}"
    }
}
