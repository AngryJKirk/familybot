package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.services.scenario.ScenarioGameplayService
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ScenarioExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.SCENARIO

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (update.message.text.contains("move")) {
            val nextMove = scenarioGameplayService.nextState(update.toChat())
            if (nextMove == null) {
                return { it.send(update, "State haven't moved") }
            } else {
                return { it.send(update, "moved state to $nextMove") }
            }
        }

        if (update.message.text.contains("story")) {
            val story = scenarioService.getAllStoryOfCurrentGame(update.toChat())

            return { it.send(update, story, enableHtml = true) }
        }

        val currentGame = scenarioService.getAllCurrentGames()[update.toChat()]
        return when {
            currentGame == null -> {
                scenarioSessionManagementService.listGames(update)
            }
            currentGame.isEnd -> {
                {
                    scenarioSessionManagementService.processCurrentGame(update).invoke(it)
                    delay(2000L)
                    scenarioSessionManagementService.listGames(update).invoke(it)
                }
            }
            else -> {
                scenarioSessionManagementService.processCurrentGame(update)
            }
        }
    }
}