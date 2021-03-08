package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import java.time.Instant
import java.util.UUID

data class ScenarioMove(
    val id: UUID,
    val description: String,
    val ways: List<ScenarioWay>,
    val isEnd: Boolean
)

data class ScenarioWay(
    val wayId: UUID,
    val description: String,
    val answerNumber: Int,
    val nextMoveId: UUID
)

data class Scenario(
    val id: UUID,
    val name: String,
    val description: String,
    val entryPoint: ScenarioMove
)

data class ScenarioMoveVoteResult(
    val scenarioMove: ScenarioMove,
    val results: Map<ScenarioWay, List<User>>
)

data class ScenarioPoll(
    val pollId: String,
    val chat: Chat,
    val createDate: Instant,
    val scenarioMove: ScenarioMove,
    val messageId: Int
)

data class ScenarioState(
    val move: ScenarioMove,
    val date: Instant
)