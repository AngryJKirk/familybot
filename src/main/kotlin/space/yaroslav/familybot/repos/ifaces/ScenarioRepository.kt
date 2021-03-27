package space.yaroslav.familybot.repos.ifaces

import io.micrometer.core.annotation.Timed
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.services.scenario.Scenario
import space.yaroslav.familybot.services.scenario.ScenarioMove
import space.yaroslav.familybot.services.scenario.ScenarioPoll
import space.yaroslav.familybot.services.scenario.ScenarioState
import space.yaroslav.familybot.services.scenario.ScenarioWay
import java.time.Instant
import java.util.UUID

interface ScenarioRepository {

    @Timed("ScenarioRepository.getScenarios")
    fun getScenarios(): List<Scenario>

    @Timed("ScenarioRepository.findMove")
    fun findMove(id: UUID): ScenarioMove?

    @Timed("ScenarioRepository.getAllCurrentGames")
    fun getAllCurrentGames(): Map<Chat, ScenarioMove>

    @Timed("ScenarioRepository.addState")
    fun addState(scenarioMove: ScenarioMove, chat: Chat)

    @Timed("ScenarioRepository.getState")
    fun getState(chat: Chat): ScenarioState?

    @Timed("ScenarioRepository.addChoice")
    fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay)

    @Timed("ScenarioRepository.removeChoice")
    fun removeChoice(chat: Chat, user: User, scenarioMove: ScenarioMove)

    @Timed("ScenarioRepository.getResultsForMove")
    fun getResultsForMove(chat: Chat, scenarioState: ScenarioState): Map<ScenarioWay, List<User>>

    @Timed("ScenarioRepository.savePoll")
    fun savePoll(scenarioPoll: ScenarioPoll)

    @Timed("ScenarioRepository.getDataByPollId")
    fun getDataByPollId(id: String): ScenarioPoll?

    @Timed("ScenarioRepository.findScenarioPoll")
    fun findScenarioPoll(chat: Chat, scenarioMove: ScenarioMove, afterDate: Instant): ScenarioPoll?

    @Timed("ScenarioRepository.allPolls")
    fun allPolls(from: Instant, to: Instant): List<ScenarioPoll>

    @Timed("ScenarioRepository.findMostRecentPoll")
    fun findMostRecentPoll(chat: Chat): ScenarioPoll?

    @Timed("ScenarioRepository.getAllStatesOfChat")
    fun getAllStatesOfChat(chat: Chat): List<ScenarioState>

    @Timed("ScenarioRepository.getCurrentMoveOfChat")
    fun getCurrentMoveOfChat(chat: Chat): ScenarioMove?

    @Timed("ScenarioRepository.getPreviousMove")
    fun getPreviousMove(move: ScenarioMove): ScenarioMove?
}
