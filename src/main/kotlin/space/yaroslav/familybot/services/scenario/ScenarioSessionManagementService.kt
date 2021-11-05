package space.yaroslav.familybot.services.scenario

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.italic
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.toHourMinuteString
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ScenarioSessionManagementService(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val scenarioPollManagingService: ScenarioPollManagingService
) {
    private val log = getLogger()

    fun startGame(executorContext: ExecutorContext, scenario: Scenario): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat

        val currentMove = scenarioGameplayService.getCurrentScenarioState(chat)
        if (currentMove != null && currentMove.move.isEnd.not()) {
            return {
                it.execute(
                    AnswerCallbackQuery().apply {
                        callbackQueryId = executorContext.update.callbackQuery.id
                        showAlert = true
                        text = executorContext.phrase(Phrase.SCENARIO_IS_RUNNING_ALREADY)
                    }
                )
            }
        }
        scenarioGameplayService.startGame(scenario, chat)
        return {
            it.send(executorContext, executorContext.phrase(Phrase.SCENARIO_IS_STARTING))
            currentGame(executorContext).invoke(it)
        }
    }

    fun processCurrentGame(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        return currentGame(executorContext)
    }

    fun listGames(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {

        return {
            it.send(
                executorContext,
                executorContext.phrase(Phrase.SCENARIO_RULES)
            )
            it.send(
                executorContext,
                executorContext.phrase(Phrase.SCENARIO_CHOOSE),
                replyToUpdate = true,
                customization = createKeyboardMarkup()
            )
        }
    }

    private fun currentGame(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat
        val previousMove = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (previousMove.isEnd) {
            return {
                val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                    ?: throw FamilyBot.InternalException("Scenario seems broken")
                it.send(executorContext, getExpositionMessage(previousMove, evenMorePreviousMove), enableHtml = true)
                delay(2000)
                it.send(executorContext, previousMove.description)
                delay(2000)
                it.send(executorContext, executorContext.phrase(Phrase.SCENARIO_END))
            }
        }
        val recentPoll = scenarioPollManagingService.getRecentPoll(chat, previousMove)
            ?: return { sender ->
                continueGame(
                    executorContext,
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
                    continueGame(executorContext, nextMove, sender, previousMove)
                } else {
                    sender.send(
                        executorContext,
                        executorContext.phrase(Phrase.SCENARIO_POLL_DRAW)
                    )
                    val evenMorePreviousMove = scenarioService.getPreviousMove(previousMove)
                    delay(2000)
                    continueGame(executorContext, previousMove, sender, evenMorePreviousMove)
                }
            } else {
                val timeLeft =
                    Duration.between(
                        Instant.now(),
                        recentPoll.createDate.plus(1, ChronoUnit.DAYS)
                    ).toHourMinuteString()

                runCatching {
                    sender.send(
                        executorContext,
                        executorContext.phrase(Phrase.SCENARIO_POLL_EXISTS).replace("\$timeLeft", timeLeft),
                        replyMessageId = recentPoll.messageId
                    )
                }
                    .onFailure { throwable ->
                        log.error("Sending poll reply fucked up", throwable)
                        sender.send(
                            executorContext,
                            executorContext.phrase(Phrase.SCENARIO_POLL_EXISTS_FALLBACK).replace("\$timeLeft", timeLeft)
                        )
                    }
            }
        }
    }

    private suspend fun continueGame(
        executorContext: ExecutorContext,
        nextMove: ScenarioMove,
        sender: AbsSender,
        previousMove: ScenarioMove?
    ) {
        if (previousMove != null) {
            sender.send(executorContext, getExpositionMessage(nextMove, previousMove), enableHtml = true)
            delay(2000)
        }

        val message = sendPoll(executorContext, nextMove).invoke(sender)
        scenarioPollManagingService.savePollToScenario(
            ScenarioPoll(
                message.poll.id,
                executorContext.chat,
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
        executorContext: ExecutorContext,
        scenarioMove: ScenarioMove
    ): suspend (AbsSender) -> Message {
        return when {
            scenarioMove.ways.any { it.description.length > 100 } -> sendSeparately(scenarioMove, executorContext)
            scenarioMove.description.length > 255 -> sendDescriptionSeparately(scenarioMove, executorContext)
            else -> sendInOneMessage(scenarioMove, executorContext)
        }
    }

    private fun sendSeparately(
        scenarioMove: ScenarioMove,
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove
            .ways
            .map(ScenarioWay::description)
            .mapIndexed { description, i -> "$i. $description" }
            .joinToString("\n")
        val messageToSend = moveDescription + "\n\n" + scenarioOptions
        return {
            val message = it.send(executorContext, messageToSend)
            it.execute(
                SendPoll().apply {
                    chatId = executorContext.chat.idString
                    question = executorContext.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = (1..scenarioMove.ways.size).map { i -> "Вариант $i" }
                    replyToMessageId = message.messageId
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.send(executorContext, moveDescription)
            it.execute(
                SendPoll().apply {
                    chatId = executorContext.chat.idString
                    question = executorContext.phrase(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = scenarioOptions
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendInOneMessage(
        scenarioMove: ScenarioMove,
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Message {
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.execute(
                SendPoll()
                    .apply {
                        chatId = executorContext.chat.idString
                        question = scenarioMove.description
                        options = scenarioOptions
                        isAnonymous = false
                    }
            )
        }
    }
}
