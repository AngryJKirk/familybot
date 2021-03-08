package space.yaroslav.familybot.services.scenario

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.italic
import space.yaroslav.familybot.repos.ifaces.ScenarioRepository

@Component
class ScenarioServiceImpl(
    private val scenarioRepository: ScenarioRepository
) : ScenarioService {

    private val log = getLogger()

    override fun getScenarios(): List<Scenario> {
        return scenarioRepository.getScenarios()
    }

    override fun getAllCurrentGames(): Map<Chat, ScenarioMove> {
        return scenarioRepository.getAllCurrentGames().also {
            log.info("All current games are $it")
        }
    }

    override fun getAllStoryOfCurrentGame(chat: Chat): String {
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