package space.yaroslav.familybot.services.scenario

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.chatIdString
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toHourMinuteString
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.services.talking.DictionaryContext
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ScenarioSessionManagementService(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val scenarioPollManagingService: ScenarioPollManagingService,
    private val dictionary: Dictionary
) {
    private val log = getLogger()

    fun startGame(update: Update, scenario: Scenario): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val context = dictionary.createContext(chat)
        val currentMove = scenarioGameplayService.getCurrentScenarioState(chat)
        if (currentMove != null && currentMove.move.isEnd.not()) {
            return {
                it.execute(
                    AnswerCallbackQuery().apply {
                        callbackQueryId = update.callbackQuery.id
                        showAlert = true
                        text = context.get(Phrase.SCENARIO_IS_RUNNING_ALREADY)
                    }
                )
            }
        }
        scenarioGameplayService.startGame(scenario, chat)
        return {
            it.send(update, context.get(Phrase.SCENARIO_IS_STARTING))
            currentGame(update, context).invoke(it)
        }
    }

    fun processCurrentGame(update: Update): suspend (AbsSender) -> Unit {
        return currentGame(update, dictionary.createContext(update))
    }

    fun listGames(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        return {
            it.send(
                update,
                context.get(Phrase.SCENARIO_RULES)
            )
            it.send(
                update, context.get(Phrase.SCENARIO_CHOOSE),
                replyToUpdate = true,
                customization = createKeyboardMarkup()
            )
        }
    }

    private fun currentGame(update: Update, context: DictionaryContext): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val previousMove = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (previousMove.isEnd) {
            return {
                val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                    ?: throw FamilyBot.InternalException("Scenario seems broken")
                it.send(update, getExpositionMessage(previousMove, evenMorePreviousMove), enableHtml = true)
                delay(2000)
                it.send(update, previousMove.description)
                delay(2000)
                it.send(update, context.get(Phrase.SCENARIO_END))
            }
        }
        val recentPoll = scenarioPollManagingService.getRecentPoll(chat, previousMove)
            ?: return { sender ->
                continueGame(
                    previousMove,
                    update,
                    sender,
                    scenarioService.getPreviousMove(previousMove),
                    context
                )
            }

        return { sender ->
            val dayAgo = Instant.now().minusMillis(Duration.of(24, ChronoUnit.HOURS).toMillis())
            if (dayAgo.isAfter(recentPoll.createDate)) {
                val nextMove = scenarioGameplayService.nextState(recentPoll.chat)
                if (nextMove != null) {
                    continueGame(nextMove, update, sender, previousMove, context)
                } else {
                    sender.send(
                        update,
                        context.get(Phrase.SCENARIO_POLL_DRAW)
                    )
                    val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                    delay(2000)
                    continueGame(previousMove, update, sender, evenMorePreviousMove, context)
                }
            } else {
                val timeLeft =
                    Duration.between(
                        Instant.now(),
                        recentPoll.createDate.plusMillis(Duration.of(1, ChronoUnit.DAYS).toMillis())
                    ).toHourMinuteString()

                runCatching {
                    sender.send(
                        update,
                        context.get(Phrase.SCENARIO_POLL_EXISTS).replace("\$timeLeft", timeLeft),
                        replyMessageId = recentPoll.messageId
                    )
                }
                    .onFailure { throwable ->
                        log.error("Sending poll reply fucked up", throwable)
                        sender.send(
                            update,
                            context.get(Phrase.SCENARIO_POLL_EXISTS_FALLBACK).replace("\$timeLeft", timeLeft)
                        )
                    }
            }
        }
    }

    private suspend fun continueGame(
        nextMove: ScenarioMove,
        update: Update,
        sender: AbsSender,
        previousMove: ScenarioMove?,
        context: DictionaryContext
    ) {
        if (previousMove != null) {
            sender.send(update, getExpositionMessage(nextMove, previousMove), enableHtml = true)
            delay(2000)
        }

        val message = sendPoll(nextMove, update, context).invoke(sender)
        scenarioPollManagingService.savePollToScenario(
            ScenarioPoll(
                message.poll.id,
                update.toChat(),
                Instant.now(),
                nextMove,
                message.messageId
            )
        )
    }

    private fun getExpositionMessage(
        nextMove: ScenarioMove,
        previousMove: ScenarioMove
    ): String {
        val chosenWay = previousMove.ways.find { it.nextMoveId == nextMove.id }
            ?: throw FamilyBot.InternalException("Wrong game logic, next move=$nextMove previous=$previousMove")
        return """
            <b>Этап истории:</b> ${previousMove.description.italic()}
            <b>Лидирующий ответ:</b> ${chosenWay.description}
        """.trimIndent()
    }

    private fun createKeyboardMarkup(): SendMessage.() -> Unit {
        return {
            replyMarkup =
                InlineKeyboardMarkup(
                    scenarioService
                        .getScenarios()
                        .chunked(1)
                        .map {
                            it.map { scenario ->
                                InlineKeyboardButton(scenario.name)
                                    .apply { callbackData = scenario.id.toString() }
                            }
                        }
                )
        }
    }

    private fun sendPoll(
        scenarioMove: ScenarioMove,
        update: Update,
        context: DictionaryContext
    ): suspend (AbsSender) -> Message {
        return when {
            scenarioMove.ways.any { it.description.length > 100 } -> sendSeparately(scenarioMove, update, context)
            scenarioMove.description.length > 255 -> sendDescriptionSeparately(scenarioMove, update, context)
            else -> sendInOneMessage(scenarioMove, update)
        }
    }

    private fun sendSeparately(
        scenarioMove: ScenarioMove,
        update: Update,
        context: DictionaryContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove
            .ways
            .map(ScenarioWay::description)
            .mapIndexed { description, i -> "$i. $description" }
            .joinToString("\n")
        val messageToSend = moveDescription + "\n\n" + scenarioOptions
        return {
            val message = it.send(update, messageToSend)
            it.execute(
                SendPoll().apply {
                    chatId = update.chatIdString()
                    question = context.get(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = (1..scenarioMove.ways.size).map { i -> "Вариант $i" }
                    replyToMessageId = message.messageId
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        update: Update,
        context: DictionaryContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.send(update, moveDescription)
            it.execute(
                SendPoll().apply {
                    chatId = update.chatIdString()
                    question = context.get(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = scenarioOptions
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendInOneMessage(
        scenarioMove: ScenarioMove,
        update: Update
    ): suspend (AbsSender) -> Message {
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.execute(
                SendPoll()
                    .apply {
                        chatId = update.chatIdString()
                        question = scenarioMove.description
                        options = scenarioOptions
                        isAnonymous = false
                    }
            )
        }
    }
}
