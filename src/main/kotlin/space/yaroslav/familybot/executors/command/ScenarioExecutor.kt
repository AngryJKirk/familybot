package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.scenario.ScenarioGameplayService
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService

@Component
class ScenarioExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService
) : CommandExecutor() {
    override fun command() = Command.SCENARIO

    companion object {
        const val MOVE_PREFIX = "move"
        const val STORY_PREFIX = "story"
    }

    override fun execute(executorContext: ExecutorContext): suspend (AbsSender) -> Unit {
        if (executorContext.message.text.contains(STORY_PREFIX)) {
            return tellTheStory(executorContext)
        }

        if (executorContext.isFromDeveloper && executorContext.message.text.contains(MOVE_PREFIX)) {
            return moveState(executorContext)
        }

        return processGame(executorContext)
    }

    private fun processGame(
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val chat = executorContext.chat
        val currentGame = scenarioService.getCurrentGame(chat)
        return when {
            currentGame == null -> {
                scenarioSessionManagementService.listGames(executorContext)
            }
            currentGame.isEnd -> {
                {
                    scenarioSessionManagementService.processCurrentGame(executorContext).invoke(it)
                    delay(2000L)
                    scenarioSessionManagementService.listGames(executorContext).invoke(it)
                }
            }
            else -> {
                scenarioSessionManagementService.processCurrentGame(executorContext)
            }
        }
    }

    private fun tellTheStory(
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val story = scenarioService.getAllStoryOfCurrentGame(executorContext.chat)
        return { it.send(executorContext, story, enableHtml = true) }
    }

    private fun moveState(
        executorContext: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val nextMove = scenarioGameplayService.nextState(executorContext.chat)
        if (nextMove == null) {
            return { it.send(executorContext, "State hasn't been moved") }
        } else {
            return {}
        }
    }
}
