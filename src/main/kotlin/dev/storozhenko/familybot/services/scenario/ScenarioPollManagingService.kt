package space.yaroslav.familybot.services.scenario

import org.springframework.stereotype.Component
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.repos.ScenarioRepository

@Component
class ScenarioPollManagingService(
    private val scenarioRepository: ScenarioRepository
) {

    private val log = getLogger()

    fun savePollToScenario(scenarioPoll: ScenarioPoll) {
        log.info("Saving new scenario poll $scenarioPoll")
        scenarioRepository.savePoll(scenarioPoll)
    }

    fun findScenarioPoll(id: String): ScenarioPoll? {
        log.info("Trying to find new scenario poll with id $id")
        return scenarioRepository.getDataByPollId(id)
            .also { log.info("Found poll: $it") }
    }

    fun getRecentPoll(chat: Chat, scenarioMove: ScenarioMove): ScenarioPoll? {
        log.info("Trying to find poll for chat $chat and move $scenarioMove")
        val mostRecentPoll = scenarioRepository.findMostRecentPoll(chat) ?: return null
        log.info("Found poll: $mostRecentPoll")
        if (mostRecentPoll.scenarioMove != scenarioMove) {
            log.info("Poll doesn't match to the required scenario")
            return null
        }
        return mostRecentPoll
    }
}
