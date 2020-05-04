package space.yaroslav.familybot.services.scenario

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger
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
}
