package space.yaroslav.familybot.services.scenario

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger

@Component
class ScenarioSchedulerService(
    private val scenarioService: ScenarioService,
    private val scenarioGameplayService: ScenarioGameplayService
) {
    private val log = getLogger()

    @Scheduled(cron = "0 0 23 * * *")
    fun moveStates() {
        runCatching {
            scenarioService.getAllCurrentGames()
                .forEach { (chat, _) -> moveState(chat) }
        }.onFailure {
            log.error("Something went wrong during state movements", it)
        }
    }

    private fun moveState(chat: Chat) {
        val currentMoveResults = scenarioGameplayService.getCurrentMoveResults(chat)
        val chosenWay = currentMoveResults.results.maxByOrNull { it.value.size } ?: return
        scenarioGameplayService.nextMove(chat, chosenWay.key)
    }
}
