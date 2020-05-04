package space.yaroslav.familybot.services.scenario

import java.time.Instant
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.telegram.FamilyBot

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
                update, "Какую игру выбрать?",
                replyToUpdate = true,
                customization = createKeyboardMarkup()
            )
        }
    }

    private fun currentGame(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val currentScenarioState = scenarioGameplayService.getCurrentScenarioState(chat)?.move
            ?: throw FamilyBot.InternalException("Internal logic error, current state wasn't found")
        if (currentScenarioState.isEnd) {
            return { it.send(update, currentScenarioState.description) }
        }
        val todayPoll = scenarioPollManagingService.getTodayPoll(chat, currentScenarioState)
        if (todayPoll == null) {
            return {
                val options = currentScenarioState.ways.map(ScenarioWay::description)
                val message = it.execute(
                    SendPoll(chat.id, currentScenarioState.description, options)
                        .setAnonymous(false)
                )
                scenarioPollManagingService.savePollToScenario(
                    ScenarioPoll(
                        message.poll.id,
                        chat,
                        Instant.now(),
                        currentScenarioState
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
                ))
        }
    }
}
