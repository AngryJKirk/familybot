package space.yaroslav.familybot.services

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.isToday
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.services.scenario.ScenarioGameplayService
import space.yaroslav.familybot.services.scenario.ScenarioPollManagingService
import space.yaroslav.familybot.telegram.FamilyBot

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
        if (poll.createDate.isToday().not()) {
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
