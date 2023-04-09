package dev.storozhenko.familybot.services.scenario

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.common.extensions.toHourMinuteString
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
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

    fun startGame(context: ExecutorContext, scenario: Scenario): suspend (AbsSender) -> Unit {
        val chat = context.chat

        val currentMove = scenarioGameplayService.getCurrentScenarioState(chat)
        if (currentMove != null && currentMove.move.isEnd.not()) {
            return {
                it.execute(
                    AnswerCallbackQuery().apply {
                        callbackQueryId = context.update.callbackQuery.id
                        showAlert = true
                        text = context.phrase(Phrase.SCENARIO_IS_RUNNING_ALREADY)
                    }
                )
            }
        }
        scenarioGameplayService.startGame(scenario, chat)
        return {
            it.send(context, context.phrase(Phrase.SCENARIO_IS_STARTING))
            currentGame(context).invoke(it)
        }
    }

    fun processCurrentGame(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return currentGame(context)
    }

    fun listGames(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return {
            it.send(
                context,
                context.phrase(Phrase.SCENARIO_RULES)
            )
            it.send(
                context,
                context.phrase(Phrase.SCENARIO_CHOOSE),
                replyToUpdate = true,
                customization = createKeyboardMarkup()
            )
        }
    }

    private fun currentGame(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val previousMove = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (previousMove.isEnd) {
            return sendFinal(previousMove, context)
        }
        val recentPoll = scenarioPollManagingService.getRecentPoll(chat, previousMove)
            ?: return { sender ->
                continueGame(
                    context,
                    previousMove,
                    sender,
                    scenarioService.getPreviousMove(previousMove)
                )
            }

        return { sender ->
            val dayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
            if (dayAgo.isAfter(recentPoll.createDate)) {
                val nextMove = scenarioGameplayService.nextState(recentPoll.chat)
                if (nextMove != null) {
                    if (nextMove.isEnd) {
                        sendFinal(nextMove, context).invoke(sender)
                    } else {
                        continueGame(context, nextMove, sender, previousMove)
                    }
                } else {
                    sender.send(
                        context,
                        context.phrase(Phrase.SCENARIO_POLL_DRAW)
                    )
                    val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                    delay(2.seconds)
                    continueGame(context, previousMove, sender, evenMorePreviousMove)
                }
            } else {
                val timeLeft =
                    Duration.between(
                        Instant.now(),
                        recentPoll.createDate.plus(1, ChronoUnit.DAYS)
                    ).toHourMinuteString()

                runCatching {
                    sender.send(
                        context,
                        context.phrase(Phrase.SCENARIO_POLL_EXISTS).replace("\$timeLeft", timeLeft),
                        replyMessageId = recentPoll.messageId
                    )
                }
                    .onFailure { throwable ->
                        log.error("Sending poll reply fucked up", throwable)
                        sender.send(
                            context,
                            context.phrase(Phrase.SCENARIO_POLL_EXISTS_FALLBACK).replace("\$timeLeft", timeLeft)
                        )
                    }
            }
        }
    }

    private suspend fun continueGame(
        context: ExecutorContext,
        nextMove: ScenarioMove,
        sender: AbsSender,
        previousMove: ScenarioMove?
    ) {
        if (previousMove != null) {
            sender.send(context, getExpositionMessage(nextMove, previousMove), enableHtml = true)
            delay(2.seconds)
        }

        val message = sendPoll(context, nextMove).invoke(sender)
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

    private fun sendPoll(
        context: ExecutorContext,
        scenarioMove: ScenarioMove
    ): suspend (AbsSender) -> Message {
        return when {
            scenarioMove.ways.any { it.description.length > 100 } -> sendSeparately(scenarioMove, context)
            scenarioMove.description.length > 255 -> sendDescriptionSeparately(scenarioMove, context)
            else -> sendInOneMessage(scenarioMove, context)
        }
    }

    private fun sendSeparately(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove
            .ways
            .map(ScenarioWay::description)
            .mapIndexed { i, description -> "${(i + 1).toString().bold()}. $description" }
            .joinToString("\n")
        val messageToSend = moveDescription + "\n\n" + scenarioOptions
        return {
            val message = it.send(context, messageToSend, enableHtml = true)
            it.execute(
                SendPoll().apply {
                    chatId = context.chat.idString
                    question = context.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = (1..scenarioMove.ways.size).map { i -> "Вариант $i" }
                    replyToMessageId = message.messageId
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.send(context, moveDescription)
            it.execute(
                SendPoll().apply {
                    chatId = context.chat.idString
                    question = context.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = scenarioOptions
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendInOneMessage(
        scenarioMove: ScenarioMove,
        context: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.execute(
                SendPoll()
                    .apply {
                        chatId = context.chat.idString
                        question = scenarioMove.description
                        options = scenarioOptions
                        isAnonymous = false
                    }
            )
        }
    }

    private fun sendFinal(previousMove: ScenarioMove, context: ExecutorContext): suspend (AbsSender) -> Unit =
        { sender ->
            val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                ?: throw FamilyBot.InternalException("Scenario seems broken")
            sender.send(context, getExpositionMessage(previousMove, evenMorePreviousMove), enableHtml = true)
            delay(2.seconds)
            sender.send(context, previousMove.description)
            delay(2.seconds)
            sender.send(context, context.phrase(Phrase.SCENARIO_END))
        }
}
