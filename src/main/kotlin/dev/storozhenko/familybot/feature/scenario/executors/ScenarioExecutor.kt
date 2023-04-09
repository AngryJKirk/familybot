package dev.storozhenko.familybot.feature.scenario.executors

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.services.scenario.ScenarioGameplayService
import dev.storozhenko.familybot.services.scenario.ScenarioService
import dev.storozhenko.familybot.services.scenario.ScenarioSessionManagementService
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

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

    override suspend fun execute(context: ExecutorContext) {
        if (context.message.text.contains(STORY_PREFIX)) {
            tellTheStory(context)
            return
        }

        if (context.isFromDeveloper && context.message.text.contains(MOVE_PREFIX)) {
            moveState(context)
            return
        }

        processGame(context)
    }

    private suspend fun processGame(context: ExecutorContext) {
        val chat = context.chat
        val currentGame = scenarioService.getCurrentGame(chat)
        when {
            currentGame == null -> {
                scenarioSessionManagementService.listGames(context)
            }

            currentGame.isEnd -> {
                scenarioSessionManagementService.processCurrentGame(context)
                delay(2.seconds)
                scenarioSessionManagementService.listGames(context)
            }

            else -> {
                scenarioSessionManagementService.processCurrentGame(context)
            }
        }
    }

    private suspend fun tellTheStory(
        context: ExecutorContext
    ) {
        val story = scenarioService.getAllStoryOfCurrentGame(context.chat)
        context.sender.send(context, story, enableHtml = true)
    }

    private suspend fun moveState(
        context: ExecutorContext
    ) {
        val nextMove = scenarioGameplayService.nextState(context.chat)
        if (nextMove == null) {
            context.sender.send(context, "State hasn't been moved")
        }
    }
}
