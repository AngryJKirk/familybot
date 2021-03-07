package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.Chat

interface ScenarioService {

    fun getScenarios(): List<Scenario>

    fun getAllCurrentGames(): Map<Chat, ScenarioMove>
}
