package dev.storozhenko.familybot.feature.scenario.services

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toHourMinuteString
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.getLogger
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

@Component
class ScenarioSessionManagementService(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val scenarioPollManagingService: ScenarioPollManagingService
) {
    private val log = getLogger()

    suspend fun startGame(context: ExecutorContext, scenario: Scenario) {
        val chat = context.chat

        val currentMove = scenarioGameplayService.getCurrentScenarioState(chat)
        if (currentMove != null && currentMove.move.isEnd.not()) {
            context.sender.execute(
                AnswerCallbackQuery().apply {
                    callbackQueryId = context.update.callbackQuery.id
                    showAlert = true
                    text = context.phrase(Phrase.SCENARIO_IS_RUNNING_ALREADY)
                }
            )
            return
        }
        scenarioGameplayService.startGame(scenario, chat)
        context.sender.send(context, context.phrase(Phrase.SCENARIO_IS_STARTING))
        currentGame(context)
    }

    suspend fun processCurrentGame(context: ExecutorContext) {
        currentGame(context)
    }

    suspend fun listGames(context: ExecutorContext) {
        context.sender.send(
            context,
            context.phrase(Phrase.SCENARIO_RULES)
        )
        context.sender.send(
            context,
            context.phrase(Phrase.SCENARIO_CHOOSE),
            replyToUpdate = true,
            customization = createKeyboardMarkup()
        )
    }

    private suspend fun currentGame(context: ExecutorContext) {
        val chat = context.chat
        val previousMove = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (previousMove.isEnd) {
            sendFinal(previousMove, context)
            return
        }
        val recentPoll = scenarioPollManagingService.getRecentPoll(chat, previousMove)
        if (recentPoll == null) {
            continueGame(
                context,
                previousMove,
                scenarioService.getPreviousMove(previousMove)
            )
            return
        }

        val dayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
        if (dayAgo.isAfter(recentPoll.createDate)) {
            val nextMove = scenarioGameplayService.nextState(recentPoll.chat)
            if (nextMove != null) {
                if (nextMove.isEnd) {
                    sendFinal(nextMove, context)
                } else {
                    continueGame(context, nextMove, previousMove)
                }
            } else {
                context.sender.send(
                    context,
                    context.phrase(Phrase.SCENARIO_POLL_DRAW)
                )
                val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                delay(2.seconds)
                continueGame(context, previousMove, evenMorePreviousMove)
            }
        } else {
            val timeLeft =
                Duration.between(
                    Instant.now(),
                    recentPoll.createDate.plus(1, ChronoUnit.DAYS)
                ).toHourMinuteString()

            runCatching {
                context.sender.send(
                    context,
                    context.phrase(Phrase.SCENARIO_POLL_EXISTS).replace("\$timeLeft", timeLeft),
                    replyMessageId = recentPoll.messageId
                )
            }
                .onFailure { throwable ->
                    log.error("Sending poll reply fucked up", throwable)
                    context.sender.send(
                        context,
                        context.phrase(Phrase.SCENARIO_POLL_EXISTS_FALLBACK).replace("\$timeLeft", timeLeft)
                    )
                }
        }
    }

    private suspend fun continueGame(
        context: ExecutorContext,
        nextMove: ScenarioMove,
        previousMove: ScenarioMove?
    ) {
        if (previousMove != null) {
            context.sender.send(context, getExpositionMessage(nextMove, previousMove), enableHtml = true)
            delay(2.seconds)
        }

        val message = sendPoll(context, nextMove)
        scenarioPollManagingService.savePollToScenario(
            ScenarioPoll(
                message.poll.id,
                context.chat,
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
                            it.map { (id, name) ->
                                InlineKeyboardButton(name)
                                    .apply { callbackData = id.toString() }
                            }
                        }
                )
        }
    }

    private suspend fun sendPoll(
        context: ExecutorContext,
        scenarioMove: ScenarioMove
    ): Message {
        return when {
            scenarioMove.ways.any { it.description.length > 100 } -> sendSeparately(scenarioMove, context)
            scenarioMove.description.length > 255 -> sendDescriptionSeparately(scenarioMove, context)
            else -> sendInOneMessage(scenarioMove, context)
        }
    }

    private suspend fun sendSeparately(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove
            .ways
            .map(ScenarioWay::description)
            .mapIndexed { i, description -> "${(i + 1).toString().bold()}. $description" }
            .joinToString("\n")
        val messageToSend = moveDescription + "\n\n" + scenarioOptions
        val message = context.sender.send(context, messageToSend, enableHtml = true)
        return context.sender.execute(
            SendPoll().apply {
                chatId = context.chat.idString
                question = context.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                options = (1..scenarioMove.ways.size).map { i -> "Вариант $i" }
                replyToMessageId = message.messageId
                isAnonymous = false
            }
        )
    }

    private suspend fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        context.sender.send(context, moveDescription)
        return context.sender.execute(
            SendPoll().apply {
                chatId = context.chat.idString
                question = context.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                options = scenarioOptions
                isAnonymous = false
            }
        )
    }

    private fun sendInOneMessage(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): Message {
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return context.sender.execute(
            SendPoll()
                .apply {
                    chatId = context.chat.idString
                    question = scenarioMove.description
                    options = scenarioOptions
                    isAnonymous = false
                }
        )
    }

    private suspend fun sendFinal(previousMove: ScenarioMove, context: ExecutorContext) {
        val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
            ?: throw FamilyBot.InternalException("Scenario seems broken")
        context.sender.send(context, getExpositionMessage(previousMove, evenMorePreviousMove), enableHtml = true)
        delay(2.seconds)
        context.sender.send(context, previousMove.description)
        delay(2.seconds)
        context.sender.send(context, context.phrase(Phrase.SCENARIO_END))
    }
}
