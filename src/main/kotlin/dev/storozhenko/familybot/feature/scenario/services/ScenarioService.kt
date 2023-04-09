package dev.storozhenko.familybot.services.scenario

import dev.storozhenko.familybot.common.extensions.bold
import dev.storozhenko.familybot.common.extensions.italic
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.feature.scenario.repos.ScenarioRepository
import org.springframework.stereotype.Component

@Component
class ScenarioService(
    private val scenarioRepository: ScenarioRepository
) {

    fun getScenarios(): List<Scenario> {
        return scenarioRepository.getScenarios()
    }

    fun getPreviousMove(scenarioMove: ScenarioMove): ScenarioMove? {
        return scenarioRepository.getPreviousMove(scenarioMove)
    }

    fun getCurrentGame(chat: Chat): ScenarioMove? {
        return scenarioRepository.getCurrentMoveOfChat(chat)
    }

    fun getAllStoryOfCurrentGame(chat: Chat): String {
        val allStates = scenarioRepository
            .getAllStatesOfChat(chat)
            .sortedByDescending(ScenarioState::date)
        val currentStates = mutableListOf<ScenarioState>()
        for (i in allStates.indices) {
            val state = allStates[i]
            if (state.move.isEnd && i != 0) {
                break
            } else {
                currentStates.add(state)
            }
        }
        if (currentStates.isEmpty()) {
            return "Вы пока не играли, попробуйте начать: ${Command.SCENARIO.command}"
        }
        return currentStates
            .sortedBy(ScenarioState::date).joinToString(separator = "\n\n") { state ->
                "Этап истории: ".bold() + state.move.description.italic() +
                        "\n" + (getStateResultsFormatted(chat, state))
            }
    }

    private fun getStateResultsFormatted(chat: Chat, state: ScenarioState): String {
        if (state.move.isEnd) {
            return "Конец".bold()
        }
        val entry = scenarioRepository.getResultsForMove(chat, state)
            .entries
            .maxByOrNull { (_, users) -> users.size } ?: return ""

        return entry
            .let { (way, _) -> "Лидирующий ответ: ".bold() + way.description.italic() }
    }
}
