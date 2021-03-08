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
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.chatIdString
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toHourMinuteString
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.dictionary.Dictionary
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ScenarioSessionManagementServiceImpl(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val scenarioPollManagingService: ScenarioPollManagingService,
    private val dictionary: Dictionary
) : ScenarioSessionManagementService {
    private val log = getLogger()

    override fun startGame(update: Update, scenario: Scenario): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val currentMove = scenarioGameplayService.getCurrentScenarioState(chat)
        if (currentMove != null && currentMove.move.isEnd.not()) {
            return {
                it.execute(
                    AnswerCallbackQuery().apply {
                        callbackQueryId = update.callbackQuery.id
                        showAlert = true
                        text = dictionary.get(Phrase.SCENARIO_IS_RUNNING_ALREADY)
                    }
                )
            }
        }
        scenarioGameplayService.startGame(scenario, chat)
        return {
            it.send(update, dictionary.get(Phrase.SCENARIO_IS_STARTING))
            currentGame(update).invoke(it)
        }
    }

    override fun processCurrentGame(update: Update): suspend (AbsSender) -> Unit {
        return currentGame(update)
    }

    override fun listGames(update: Update): suspend (AbsSender) -> Unit {
        return {
            it.send(
                update,
                dictionary.get(Phrase.SCENARIO_RULES)
            )
            it.send(
                update, dictionary.get(Phrase.SCENARIO_CHOOSE),
                replyToUpdate = true,
                customization = createKeyboardMarkup()
            )
        }
    }

    private fun currentGame(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val currentScenarioMove = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (currentScenarioMove.isEnd) {
            return {
                it.send(update, currentScenarioMove.description)
                delay(2000)
                it.send(update, dictionary.get(Phrase.SCENARIO_END))
            }
        }
        val recentPoll = scenarioPollManagingService.getRecentPoll(chat, currentScenarioMove)
            ?: return { sender -> continueGame(currentScenarioMove, update, sender) }

        return { sender ->
            val dayAgo = Instant.now().minusMillis(Duration.of(24, ChronoUnit.HOURS).toMillis())
            if (dayAgo.isAfter(recentPoll.createDate)) {
                val nextMove = scenarioGameplayService.nextState(recentPoll.chat)
                if (nextMove != null) {
                    continueGame(nextMove, update, sender, currentScenarioMove)
                } else {
                    sender.send(
                        update,
                        dictionary.get(Phrase.SCENARIO_POLL_DRAW)
                    )
                    delay(2000)
                    continueGame(currentScenarioMove, update, sender, currentScenarioMove)
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
                        dictionary.get(Phrase.SCENARIO_POLL_EXISTS).replace("\$timeLeft", timeLeft),
                        replyMessageId = recentPoll.messageId
                    )
                }
                    .onFailure { throwable ->
                        log.error("Sending poll reply fucked up", throwable)
                        sender.send(
                            update,
                            dictionary.get(Phrase.SCENARIO_POLL_EXISTS_FALLBACK).replace("\$timeLeft", timeLeft)
                        )
                    }
            }
        }
    }

    private suspend fun continueGame(
        currentScenarioMove: ScenarioMove,
        update: Update,
        sender: AbsSender,
        previousScenarioMove: ScenarioMove? = null
    ) {
        if (previousScenarioMove != null) {
            sender.send(update, getExpositionMessage(currentScenarioMove, previousScenarioMove), enableHtml = true)
            delay(1000)
        }

        val message = sendPoll(currentScenarioMove, update).invoke(sender)
        scenarioPollManagingService.savePollToScenario(
            ScenarioPoll(
                message.poll.id,
                update.toChat(),
                Instant.now(),
                currentScenarioMove,
                message.messageId
            )
        )
    }

    private suspend fun getExpositionMessage(
        currentScenarioMove: ScenarioMove,
        previousScenarioMove: ScenarioMove
    ): String {
        val chosenWay = previousScenarioMove.ways.find { it.nextMoveId == currentScenarioMove.id }
            ?: throw FamilyBot.InternalException("Wrong game logic, current move=$currentScenarioMove previous=$previousScenarioMove")
        return """
            Ваше предыдущее действие:
            ${previousScenarioMove.description}
            ${previousScenarioMove.ways.map { formatAnswers(it, chosenWay) }}
        """.trimIndent()
    }

    private fun formatAnswers(way: ScenarioWay, chosenWay: ScenarioWay): String {
        val commonTemplate = "${way.answerNumber}. ${way.description}"
        return if (way.wayId == chosenWay.wayId) {
            "$commonTemplate  ❗".bold()
        } else {
            commonTemplate
        }
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

    private fun sendPoll(scenarioMove: ScenarioMove, update: Update): suspend (AbsSender) -> Message {
        return when {
            scenarioMove.ways.any { it.description.length > 100 } -> sendSeparately(scenarioMove, update)
            scenarioMove.description.length > 255 -> sendDescriptionSeparately(scenarioMove, update)
            else -> sendInOneMessage(scenarioMove, update)
        }
    }

    private fun sendSeparately(
        scenarioMove: ScenarioMove,
        update: Update
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
                    question = dictionary.get(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
                    options = (1..scenarioMove.ways.size).map { i -> "Вариант $i" }
                    replyToMessageId = message.messageId
                    isAnonymous = false
                }
            )
        }
    }

    private fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        update: Update
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val scenarioOptions = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.send(update, moveDescription)
            it.execute(
                SendPoll().apply {
                    chatId = update.chatIdString()
                    question = dictionary.get(Phrase.SCENARIO_POLL_DEFAULT_QUESTION)
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