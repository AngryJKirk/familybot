package space.yaroslav.familybot.executors.command

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.common.utils.from
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.scenario.ScenarioGameplayService
import space.yaroslav.familybot.services.scenario.ScenarioService
import space.yaroslav.familybot.services.scenario.ScenarioSessionManagementService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class ScenarioExecutor(
    private val scenarioSessionManagementService: ScenarioSessionManagementService,
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val botConfig: BotConfig
) : CommandExecutor(botConfig) {
    override fun command() = Command.SCENARIO

    companion object {
        const val MOVE_PREFIX = "move"
        const val STORY_PREFIX = "story"
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        if (update.message.text.contains(STORY_PREFIX)) {
            return tellTheStory(chat, update)
        }

        if (update.message.text.contains(MOVE_PREFIX) && botConfig.developer == update.from().userName) {
            return moveState(chat, update)
        }

        return processGame(chat, update)
    }

    private fun processGame(
        chat: Chat,
        update: Update
    ): suspend (AbsSender) -> Unit {
        val currentGame = scenarioService.getCurrentGame(chat)
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

    private fun tellTheStory(
        chat: Chat,
        update: Update
    ): suspend (AbsSender) -> Unit {
        val story = scenarioService.getAllStoryOfCurrentGame(chat)
        return { it.send(update, story, enableHtml = true) }
    }

    private fun moveState(
        chat: Chat,
        update: Update
    ): suspend (AbsSender) -> Unit {
        val nextMove = scenarioGameplayService.nextState(chat)
        if (nextMove == null) {
            return { it.send(update, "State hasn't been moved") }
        } else {
            return {}
        }
    }
}
