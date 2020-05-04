package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.services.scenario.ScenarioSchedulerService
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ScenarioExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val scenarioSchedulerService: ScenarioSchedulerService,
    botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.SCENARIO

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        if (update.message.text.contains("move")) {
            scenarioSchedulerService.moveStates()
            return { it.send(update, "moved states") }
        }

        val currentGame = scenarioService.getAllCurrentGames()[update.toChat()]
        return when {
            currentGame == null -> {
                scenarioSessionManagementService.listGames(update)
            }
            currentGame.isEnd -> {
                {
                    scenarioSessionManagementService.processCurrentGame(update).invoke(it)
                    scenarioSessionManagementService.listGames(update).invoke(it)
                }
            }
            else -> {
                scenarioSessionManagementService.processCurrentGame(update)
            }
        }
    }
}
