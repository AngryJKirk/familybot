package dev.storozhenko.familybot.services.routers

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.services.scenario.ScenarioGameplayService
import dev.storozhenko.familybot.services.scenario.ScenarioPollManagingService
import dev.storozhenko.familybot.telegram.FamilyBot
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PollRouter(
    private val scenarioPollManagingService: ScenarioPollManagingService,
    private val scenarioGameplayService: ScenarioGameplayService
) {
    private val log = getLogger()

    fun proceed(update: Update) {
        val answer = update.pollAnswer
        val poll = scenarioPollManagingService.findScenarioPoll(answer.pollId)
            ?: return
        if (poll.createDate.isBefore(Instant.now().minus(1, ChronoUnit.DAYS))) {
            return
        }
        log.info("Trying to proceed poll $poll, answer is $answer")
        val scenarioMove = poll.scenarioMove
        val chat = poll.chat
        val user = answer.user.toUser(chat = chat)
        if (answer.optionIds.isNotEmpty()) {
            val scenarioWay = scenarioMove.ways.find { it.answerNumber == answer.optionIds.first() }
                ?: throw FamilyBot.InternalException("Can't find proper answer")
            scenarioGameplayService.addChoice(chat, user, scenarioMove, scenarioWay)
        } else {
            scenarioGameplayService.removeChoice(chat, user, scenarioMove)
        }
    }
}
