package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.scenario.services.ScenarioGameplayService
import dev.storozhenko.familybot.feature.scenario.services.ScenarioPollManagingService
import dev.storozhenko.familybot.feature.settings.models.StoryCurrentPollResults
import dev.storozhenko.familybot.feature.story.PollResults
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class PollRouter(
    private val scenarioPollManagingService: ScenarioPollManagingService,
    private val scenarioGameplayService: ScenarioGameplayService,
    private val easyKeyValueService: EasyKeyValueService,
) {
    private val log = KotlinLogging.logger { }

    fun proceed(update: Update) {
        val answer = update.pollAnswer

        val story = easyKeyValueService
            .getAllByPartKey(StoryCurrentPollResults)
            .filterValues { it.pollId == answer.pollId }
            .toList()
            .firstOrNull()


        if (story != null) {
            processStory(answer, story)
        } else {
            processScenario(answer)
        }
    }

    private fun processStory(answer: PollAnswer, story: Pair<ChatEasyKey, PollResults>) {
        val userId = answer.user.id
        val (chatEasyKey, pollResults) = story
        if (answer.optionIds.isEmpty()) {
            pollResults.pollResults.forEach { (_, results) -> results.remove(userId) }
        } else {
            pollResults.pollResults.computeIfAbsent(answer.optionIds.first() + 1) { mutableSetOf() }.add(userId)
        }
        easyKeyValueService.put(StoryCurrentPollResults, chatEasyKey, pollResults)
    }

    private fun processScenario(answer: PollAnswer) {
        val poll = scenarioPollManagingService.findScenarioPoll(answer.pollId)
            ?: return
        if (poll.createDate.isBefore(Instant.now().minus(1, ChronoUnit.DAYS))) {
            return
        }
        log.info { "Trying to proceed poll $poll, answer is $answer" }
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
