package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.Chat

interface ScenarioService {

    fun getScenarios(): List<Scenario>
    fun getAllCurrentGames(): Map<Chat, ScenarioMove>
    fun getCurrentGame(chat: Chat): ScenarioMove?
    fun getAllStoryOfCurrentGame(chat: Chat): String
    fun getPreviousMove(scenarioMove: ScenarioMove): ScenarioMove?
}