package dev.storozhenko.familybot.feature.story

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.telegram.FamilyBot
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import dev.storozhenko.familybot.feature.settings.models.StoryCurrentPollResults
import dev.storozhenko.familybot.feature.settings.models.StoryGameActive
import dev.storozhenko.familybot.feature.settings.models.StoryPollBlocked
import dev.storozhenko.familybot.feature.settings.models.StoryPollsCounter
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class StoryCommandExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    private val storyTellingService: StoryTellingService
) : CommandExecutor() {
    override fun command() = Command.STORY

    override suspend fun execute(context: ExecutorContext) {
        if (context.message.text.contains("reset")) {
            listOf(
                StoryGameActive,
                StoryPollBlocked,
                StoryPollsCounter,
                StoryCurrentPollResults
            ).forEach { easyKeyValueService.remove(it, context.chatKey) }
        }
        val paidTill = easyKeyValueService.get(ChatGPTPaidTill, context.chatKey, Instant.MIN)
        if (paidTill.isBefore(Instant.now())) {
            context.client.send(context, "Игра работает только на оплаченном ИИ, велком в /shop")
        } else {
            val isGameActive = easyKeyValueService.get(StoryGameActive, context.chatKey, false)
            if (isGameActive) {
                val storyPollUnblockTime = easyKeyValueService.get(StoryPollBlocked, context.chatKey, Instant.MIN)
                if (storyPollUnblockTime.isAfter(Instant.now())) {
                    val minutesLeft = Duration.between(Instant.now(), storyPollUnblockTime).toMinutes()
                    context.client.send(
                        context,
                        "Следующее голосование будет доступно через $minutesLeft мин"
                    )
                } else {
                    val currentPollResults = easyKeyValueService.get(StoryCurrentPollResults, context.chatKey)
                        ?: throw FamilyBot.InternalException("No poll found")
                    val choice = currentPollResults.pollResults.toList().maxByOrNull { it.second.size }?.first
                        ?: throw FamilyBot.InternalException("No results found")
                    storyTellingService.continueStory(context, choice)
                }
            } else {
                context.client.send(context, "Расскажи затравку истории в двух-трех предложениях (ответом на это сообщение). История продлится около 5 шагов")
            }
        }
    }
}