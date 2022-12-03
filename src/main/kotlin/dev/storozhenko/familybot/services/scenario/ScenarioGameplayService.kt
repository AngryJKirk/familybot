package dev.storozhenko.familybot.services.scenario

import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.models.telegram.User
import dev.storozhenko.familybot.repos.ScenarioRepository
import dev.storozhenko.familybot.telegram.FamilyBot
import org.springframework.stereotype.Component

@Component
class ScenarioGameplayService(
    private val scenarioRepository: ScenarioRepository
) {
    private val log = getLogger()
    fun startGame(scenario: Scenario, chat: Chat) {
        scenarioRepository.addState(scenario.entryPoint, chat)
    }

    fun getCurrentScenarioState(chat: Chat): ScenarioState? {
        return scenarioRepository.getState(chat)
            .also { log.info("Getting scenario state for chat $chat, state is $it") }
    }

    fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay) {
        log.info("Adding choice, move is $scenarioMove, chosen way is $chosenWay, user is $user, chat is $chat")
        scenarioRepository.addChoice(chat, user, scenarioMove, chosenWay)
    }

    fun removeChoice(chat: Chat, user: User, scenarioMove: ScenarioMove) {
        log.info("Removing choice, move is $scenarioMove, user is $user, chat is $chat")
        scenarioRepository.removeChoice(chat, user, scenarioMove)
    }

    fun nextState(chat: Chat): ScenarioMove? {
        val currentMoveResults = getCurrentMoveResults(chat)
        val results: List<Pair<ScenarioWay, List<User>>> = currentMoveResults.results
            .toList()
            .sortedByDescending { (_, users) -> users.size }
        return when (results.size) {
            0 -> null
            1 -> nextMove(chat, results.first().first)
            else -> if (isAmbiguousChoice(results).not()) nextMove(chat, results.first().first) else null
        }
    }

    private fun isAmbiguousChoice(results: List<Pair<ScenarioWay, List<User>>>): Boolean {
        return results[0].second.size == results[1].second.size
    }

    private fun nextMove(chat: Chat, scenarioWay: ScenarioWay): ScenarioMove {
        log.info("Going for next move for chat $chat, way is $scenarioWay")
        val move = scenarioRepository.findMove(scenarioWay.nextMoveId)
            ?: throw FamilyBot.InternalException("Can't find a next move of the way")
        log.info("Next move is $move for chat $chat")
        scenarioRepository.addState(move, chat)
        return move
    }

    private fun getCurrentMoveResults(chat: Chat): ScenarioMoveVoteResult {
        val scenarioState = scenarioRepository.getState(chat)
            ?: throw FamilyBot.InternalException("Can't get state for the chat")
        val scenarioMove = scenarioState.move
        return if (scenarioMove.isEnd || scenarioMove.ways.isEmpty()) {
            log.info("Scenario $scenarioMove is ended of has no ways")
            ScenarioMoveVoteResult(scenarioMove, emptyMap())
        } else {
            val result: Map<ScenarioWay, List<User>> = scenarioRepository.getResultsForMove(chat, scenarioState)
            ScenarioMoveVoteResult(scenarioMove, result)
        }.also {
            log.info("Results for chat $chat is $it")
        }
    }
}
