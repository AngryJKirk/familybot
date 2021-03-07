package space.yaroslav.familybot.services.scenario

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.startOfDay
import space.yaroslav.familybot.repos.ifaces.ScenarioRepository
import java.time.Instant

@Component
class ScenarioPollManagingServiceImpl(
    private val scenarioRepository: ScenarioRepository
) : ScenarioPollManagingService {

    private val log = getLogger()

    override fun savePollToScenario(scenarioPoll: ScenarioPoll) {
        log.info("Saving new scenario poll $scenarioPoll")
        scenarioRepository.savePoll(scenarioPoll)
    }

    override fun findScenarioPoll(id: String): ScenarioPoll? {
        log.info("Trying to find new scenario poll with id $id")
        return scenarioRepository.getDataByPollId(id)
            .also { log.info("Found poll: $it") }
    }

    override fun getTodayPoll(chat: Chat, scenarioMove: ScenarioMove): ScenarioPoll? {
        log.info("Trying to find poll for chat $chat and move $scenarioMove")
        return scenarioRepository.findScenarioPoll(
            chat,
            scenarioMove,
            afterDate = Instant.now().startOfDay()
        )
            .also { log.info("Found poll: $it") }
    }
}
