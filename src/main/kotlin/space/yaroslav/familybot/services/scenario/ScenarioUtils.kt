package space.yaroslav.familybot.services.scenario

import space.yaroslav.familybot.common.utils.bold
import space.yaroslav.familybot.telegram.FamilyBot

fun formatStory(
    currentScenarioMove: ScenarioMove,
    previousScenarioMove: ScenarioMove
): String {
    val chosenWay = previousScenarioMove.ways.find { it.nextMoveId == currentScenarioMove.id }
        ?: throw FamilyBot.InternalException("Wrong game logic, current move=$currentScenarioMove previous=$previousScenarioMove")
    return """
            ${previousScenarioMove.description}
            ${previousScenarioMove.ways.map { formatAnswers(it, chosenWay) }}
    """.trimIndent()
}

private fun formatAnswers(way: ScenarioWay, chosenWay: ScenarioWay): String {
    val commonTemplate = "${way.answerNumber}. ${way.description}"
    return if (way.wayId == chosenWay.wayId) {
        "$commonTemplate  ❗".bold()
    } else {
        commonTemplate
    }
}
