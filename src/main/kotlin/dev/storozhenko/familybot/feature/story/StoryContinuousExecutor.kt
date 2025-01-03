package dev.storozhenko.familybot.feature.story

import dev.storozhenko.familybot.BotConfig

import dev.storozhenko.familybot.core.executors.ContinuousConversationExecutor
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.StoryGameActive
import org.springframework.stereotype.Component


@Component
class StoryContinuousExecutor(
    private val botConfig: BotConfig,
    private val easyKeyValueService: EasyKeyValueService,
    private val storyTellingService: StoryTellingService
) : ContinuousConversationExecutor(botConfig) {
    override fun getDialogMessages(context: ExecutorContext) =
        setOf("Расскажи затравку истории в двух-трех предложениях (ответом на это сообщение). История продлится около 5 шагов")

    override fun command() = Command.STORY

    override fun canExecute(context: ExecutorContext): Boolean {
        val message = context.message
        return message.isReply &&
                message.replyToMessage.from.userName == botConfig.botName &&
                (message.replyToMessage.text ?: "") in getDialogMessages(context)
    }

    override suspend fun execute(context: ExecutorContext) {
        val isGameActive = easyKeyValueService.get(StoryGameActive, context.chatKey, false)
        if (isGameActive) {
            context.send("Игра уже начата, дурачок, просто вызови /story")
            return
        }
        storyTellingService.initStory(context)
    }
}