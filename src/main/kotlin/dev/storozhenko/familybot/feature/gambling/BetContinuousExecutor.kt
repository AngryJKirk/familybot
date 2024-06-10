package dev.storozhenko.familybot.feature.gambling

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.untilNextMonth
import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.dictionary.Pluralization
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.pidor.models.Pidor
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import dev.storozhenko.familybot.feature.pidor.services.PidorCompetitionService
import dev.storozhenko.familybot.feature.settings.models.BetTolerance
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendDice
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.seconds

@Component
class BetContinuousExecutor(
    private val pidorRepository: PidorRepository,
    private val pidorCompetitionService: PidorCompetitionService,
    private val easyKeyValueService: EasyKeyValueService,
    private val botConfig: BotConfig,
) : ContinuousConversationExecutor(botConfig) {
    private val diceNumbers = listOf(1, 2, 3, 4, 5, 6)

    override fun getDialogMessages(context: ExecutorContext): Set<String> {
        return context.allPhrases(Phrase.BET_INITIAL_MESSAGE)
    }

    override fun command() = Command.BET

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return message.isReply &&
                message.replyToMessage.from.userName == botConfig.botName &&
                (message.replyToMessage.text ?: "") in getDialogMessages(context)
    }

    override suspend fun execute(context: ExecutorContext) {
        val user = context.user
        val chatId = context.message.chatId
        val key = context.userAndChatKey

        if (isBetAlreadyDone(key)) {
            context.client.send(context, context.phrase(Phrase.BET_ALREADY_WAS), shouldTypeBeforeSend = true)
            return
        }
        val number = extractBetNumber(context)
        if (number == null || number !in 1..3) {
            context.client.send(
                context,
                context.phrase(Phrase.BET_BREAKING_THE_RULES_FIRST),
                shouldTypeBeforeSend = true,
            )
            context.client.send(
                context,
                context.phrase(Phrase.BET_BREAKING_THE_RULES_SECOND),
                shouldTypeBeforeSend = true,
            )
            return
        }
        val winnableNumbers = diceNumbers.shuffled().subList(0, 3)
        context.client.send(
            context,
            "${context.phrase(Phrase.BET_WINNABLE_NUMBERS_ANNOUNCEMENT)} ${formatWinnableNumbers(winnableNumbers)}",
            shouldTypeBeforeSend = true,
        )
        context.client.send(context, context.phrase(Phrase.BET_ZATRAVOCHKA), shouldTypeBeforeSend = true)
        val diceMessage = context.client.execute(SendDice(chatId.toString()))
        delay(4.seconds)
        val isItWinner = winnableNumbers.contains(diceMessage.dice.value)
        if (isItWinner) {
            coroutineScope { launch { repeat(number) { pidorRepository.removePidorRecord(user) } } }
            context.client.send(context, context.phrase(Phrase.BET_WIN), shouldTypeBeforeSend = true)
            context.client.send(context, winEndPhrase(number, context), shouldTypeBeforeSend = true)
        } else {
            coroutineScope { launch { addPidorsMultiplyTimesWithDayShift(number, user) } }
            context.client.send(context, context.phrase(Phrase.BET_LOSE), shouldTypeBeforeSend = true)
            context.client.send(context, explainPhrase(number, context), shouldTypeBeforeSend = true)
        }
        easyKeyValueService.put(BetTolerance, key, true, untilNextMonth())
        delay(2.seconds)
        pidorCompetitionService.pidorCompetition(context.chat, context.chatKey).invoke(context.client)
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
                        .toInstant(ZoneOffset.UTC),
                ),
            )
            i--
        }
    }

    private fun extractBetNumber(context: ExecutorContext) =
        context.message.text.split(" ")[0].toIntOrNull()

    private fun isBetAlreadyDone(key: UserAndChatEasyKey) =
        easyKeyValueService.get(BetTolerance, key, false)

    private fun winEndPhrase(betNumber: Int, context: ExecutorContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val winPhraseTemplate = context.phrase(Phrase.BET_WIN_END)
        return when (plur) {
            Pluralization.ONE -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.phrase(Phrase.PLURALIZED_PIDORSKOE_ONE))
                    .replace("$2", context.phrase(Phrase.PLURALIZED_OCHKO_ONE))
            }

            Pluralization.FEW -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.phrase(Phrase.PLURALIZED_PIDORSKOE_FEW))
                    .replace("$2", context.phrase(Phrase.PLURALIZED_OCHKO_FEW))
            }

            Pluralization.MANY -> {
                winPhraseTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.phrase(Phrase.PLURALIZED_PIDORSKOE_MANY))
                    .replace("$2", context.phrase(Phrase.PLURALIZED_OCHKO_MANY))
            }
        }
    }

    private fun explainPhrase(betNumber: Int, context: ExecutorContext): String {
        val plur = Pluralization.getPlur(betNumber)
        val explainTemplate = context.phrase(Phrase.BET_EXPLAIN)
        return when (plur) {
            Pluralization.ONE -> {
                context.phrase(Phrase.BET_EXPLAIN_SINGLE_DAY)
            }

            else -> {
                explainTemplate
                    .replace("$0", betNumber.toString())
                    .replace("$1", context.phrase(Phrase.PLURALIZED_NEXT_MANY))
                    .replace("$2", context.phrase(Phrase.PLURALIZED_DAY_MANY))
            }
        }
    }

    private fun formatWinnableNumbers(numbers: List<Int>): String {
        val orderedNumbers = numbers.sorted()
        return "${orderedNumbers[0]}, ${orderedNumbers[1]} и ${orderedNumbers[2]}"
    }
}
