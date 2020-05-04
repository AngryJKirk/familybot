package space.yaroslav.familybot.services.scenario

import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

interface ScenarioSessionManagementService {

    fun startGame(update: Update, scenario: Scenario): suspend (AbsSender) -> Unit

    fun processCurrentGame(update: Update): suspend (AbsSender) -> Unit

    fun listGames(update: Update): suspend (AbsSender) -> Unit
}
