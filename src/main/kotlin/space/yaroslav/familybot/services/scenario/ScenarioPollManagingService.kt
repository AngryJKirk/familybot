package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.Chat

interface ScenarioPollManagingService {

    fun savePollToScenario(scenarioPoll: ScenarioPoll)

    fun findScenarioPoll(id: String): ScenarioPoll?

    fun getRecentPoll(chat: Chat, scenarioMove: ScenarioMove): ScenarioPoll?
}