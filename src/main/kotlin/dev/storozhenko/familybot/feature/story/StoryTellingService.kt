package dev.storozhenko.familybot.feature.story

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.StoryCurrentPollResults
import dev.storozhenko.familybot.feature.settings.models.StoryGameActive
import dev.storozhenko.familybot.feature.settings.models.StoryPollBlocked
import dev.storozhenko.familybot.feature.settings.models.StoryPollsCounter
import dev.storozhenko.familybot.feature.talking.services.TalkingServiceChatGpt
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption
import java.time.Instant
import java.time.temporal.ChronoUnit

@Suppress("MoveLambdaOutsideParentheses")
@Component
class StoryTellingService(
    private val talkingService: TalkingServiceChatGpt,
    private val easyKeyValueService: EasyKeyValueService
) {
    private val messageStory: MutableMap<Long, MutableList<String>> = mutableMapOf()
    suspend fun initStory(context: ExecutorContext) {
        val initMessage = """
            Ты чат бот, ведущий текстовой игры. Игроки начинают игру сообщением, которое описывает контекст или затравку.
            Твоя задача развить историю в несколько предложений, создать ситуацию и предложить игрокам выбрать что они будут делать.
            Продолжение истории должно быть по стилю связанное с изначальной ситуацией, иными словами должен повторяться стиль и подача.
            Варианты исходов событий должны быть в одно предложение. 
            Пришли три нумерованных варианта развитий событий.
            
            Вот контекст присланный игроками: "${context.message.text}"
        """.trimIndent()
        val initResponse = talkingService.internalMessage(initMessage, useGpt4 = true)
        addResponse(context, initResponse)
        sendPoll(context, initResponse)
        easyKeyValueService.put(StoryPollsCounter, context.chatKey, 1)

    }

    suspend fun continueStory(context: ExecutorContext, previousAnswer: Int) {
        val isEndOfStory = easyKeyValueService.get(StoryPollsCounter, context.chatKey, 1) >= 5
        val message = if (isEndOfStory) {
            """
            Игроки выбрали ответ номер $previousAnswer
            Следующий этап истории это финал (3-4 предложения). Напиши заключение и серую мораль, возможно с острыми поворотами сюжета.
        """.trimIndent()
        } else {
            """
            Игроки выбрали ответ номер $previousAnswer
            Продолжи историю (3-4 предложения) и снова пришли три варианта продолжения событий.
            В начале сообщения напиши выбранный игроками ответ в формате "Игроки выбрали <текст ответа>".
        """.trimIndent()
        }

        val previousMessages = (messageStory[context.chat.id] ?: emptyList()).joinToString("\n\n")
        val continueStoryResponse = talkingService.internalMessage(previousMessages + "\n\n" + message, useGpt4 = true)
        if (isEndOfStory) {
            cleanUp(context)
            context.client.send(context, continueStoryResponse)
        } else {
            addResponse(context, continueStoryResponse)
            sendPoll(context, continueStoryResponse)
            easyKeyValueService.increment(StoryPollsCounter, context.chatKey)
        }
    }

    private fun addResponse(
        context: ExecutorContext,
        initResponse: String
    ) {
        messageStory.computeIfAbsent(context.chat.id, { mutableListOf() })
            .add("Вот история присланная тобою ранее: $initResponse")
    }

    private fun cleanUp(context: ExecutorContext) {
        easyKeyValueService.remove(StoryGameActive, context.chatKey)
        easyKeyValueService.remove(StoryPollsCounter, context.chatKey)
        easyKeyValueService.remove(StoryCurrentPollResults, context.chatKey)
    }

    private suspend fun sendPoll(context: ExecutorContext, response: String) {
        context.client.send(context, response)
        val poll = context.client.execute(
            SendPoll(
                context.chat.idString, "Какой вариант выбираете?",
                listOf(InputPollOption("1"), InputPollOption("2"), InputPollOption("3"))
            )
                .apply {
                    isAnonymous = false
                }
        )
        easyKeyValueService.put(StoryGameActive, context.chatKey, true)
        easyKeyValueService.put(StoryPollBlocked, context.chatKey, Instant.now().plus(5 , ChronoUnit.MINUTES))
        easyKeyValueService.put(StoryCurrentPollResults, context.chatKey, PollResults(poll.poll.id))
    }
}