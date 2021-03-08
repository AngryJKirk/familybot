package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User

interface ScenarioGameplayService {

    fun startGame(scenario: Scenario, chat: Chat)

    fun getCurrentScenarioState(chat: Chat): ScenarioState?

    fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay)

    fun removeChoice(chat: Chat, user: User, scenarioMove: ScenarioMove)

    fun nextState(chat: Chat): ScenarioMove?
}
