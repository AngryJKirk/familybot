package space.yaroslav.familybot.services.scenario

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.chatId
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant

@Component
class ScenarioSessionManagementServiceImpl(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val scenarioPollManagingService: ScenarioPollManagingService
) : ScenarioSessionManagementService {

    override fun startGame(update: Update, scenario: Scenario): suspend (AbsSender) -> Unit {
        scenarioGameplayService.startGame(scenario, update.toChat())
        return {
            it.send(update, "Игра начинается!")
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
                "Какую игру выбрать?",
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
                it.send(update, "Охуеть, вы дошли до конца игры! Награда как всегда, нихуя. Можете начать следующую.")
            }
        }
        val todayPoll = scenarioPollManagingService.getTodayPoll(chat, currentScenarioMove)
        if (todayPoll == null) {
            return {
                val message = sendPoll(currentScenarioMove, update).invoke(it)
                scenarioPollManagingService.savePollToScenario(
                    ScenarioPoll(
                        message.poll.id,
                        chat,
                        Instant.now(),
                        currentScenarioMove
                    )
                )
            }
        } else {
            return {
                it.send(update, "На сегодня хватит, ждите завтра")
            }
        }
    }

    private fun createKeyboardMarkup(): SendMessage.() -> SendMessage {
        return {
            setReplyMarkup(
                InlineKeyboardMarkup(
                    scenarioService.getScenarios().chunked(2)
                        .map {
                            it.map { scenario ->
                                InlineKeyboardButton(scenario.name)
                                    .setCallbackData(scenario.id.toString())
                            }
                        }
                )
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
        val options = scenarioMove
            .ways
            .map(ScenarioWay::description)
            .mapIndexed { description, i -> "$i. $description" }
            .joinToString("\n")
        val messageToSend = moveDescription + "\n\n" + options
        return {
            val message = it.send(update, messageToSend)
            it.execute(
                SendPoll()
                    .setChatId(update.chatId())
                    .setQuestion("Что выбираете?")
                    .setOptions((1..scenarioMove.ways.size).map { i -> "Вариант $i" })
                    .setReplyToMessageId(message.messageId)

                    .setAnonymous(false)
            )
        }
    }

    private fun sendDescriptionSeparately(
        scenarioMove: ScenarioMove,
        update: Update
    ): suspend (AbsSender) -> Message {
        val moveDescription = scenarioMove.description
        val options = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.send(update, moveDescription)
            it.execute(
                SendPoll()
                    .setChatId(update.chatId())
                    .setQuestion("Что выбираете?")
                    .setOptions(options)
                    .setAnonymous(false)
            )
        }
    }

    private fun sendInOneMessage(
        scenarioMove: ScenarioMove,
        update: Update
    ): suspend (AbsSender) -> Message {
        val options = scenarioMove.ways.map(ScenarioWay::description)
        return {
            it.execute(
                SendPoll()
                    .setChatId(update.chatId())
                    .setQuestion(scenarioMove.description)
                    .setOptions(options)
                    .setAnonymous(false)
            )
        }
    }
}
