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

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (context.message.text.contains(STORY_PREFIX)) {
            return tellTheStory(context)
        }

        if (context.isFromDeveloper && context.message.text.contains(MOVE_PREFIX)) {
            return moveState(context)
        }

        return processGame(context)
    }

    private fun processGame(
        context: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val chat = context.chat
        val currentGame = scenarioService.getCurrentGame(chat)
        return when {
            currentGame == null -> {
                scenarioSessionManagementService.listGames(context)
            }
            currentGame.isEnd -> {
                {
                    scenarioSessionManagementService.processCurrentGame(context).invoke(it)
                    delay(2000L)
                    scenarioSessionManagementService.listGames(context).invoke(it)
                }
            }
            else -> {
                scenarioSessionManagementService.processCurrentGame(context)
            }
        }
    }

    private fun tellTheStory(
        context: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val story = scenarioService.getAllStoryOfCurrentGame(context.chat)
        return { it.send(context, story, enableHtml = true) }
    }

    private fun moveState(
        context: ExecutorContext
    ): suspend (AbsSender) -> Unit {
        val nextMove = scenarioGameplayService.nextState(context.chat)
        if (nextMove == null) {
            return { it.send(context, "State hasn't been moved") }
        } else {
            return {}
        }
    }
}
