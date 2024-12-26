package dev.storozhenko.familybot.feature.talking.services.keyword.processor


import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.PshenitsinTolerance
import dev.storozhenko.familybot.feature.talking.services.TalkingService
import dev.storozhenko.familybot.feature.talking.services.keyword.KeyWordProcessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class PshenitsinKeyWordProcessor(
    @Qualifier("Old") private val talkingService: TalkingService,
    private val keyValueService: EasyKeyValueService,
) : KeyWordProcessor {

    private val spaceSplit = Regex("\\s+")
    override fun canProcess(context: ExecutorContext): Boolean {
        val text = context.message.text ?: return false
        return containsSymbolsY(text) && isTolerant(context.message.chatId).not()
    }

    override suspend fun process(context: ExecutorContext) {
        val text = talkingService
            .getReplyToUser(context)
            .toCharArray()
            .map { ch ->
                when {
                    ch.isLetter() && ch.isUpperCase() -> 'Ы'
                    ch.isLetter() && ch.isLowerCase() -> 'ы'
                    else -> ch
                }
            }
            .toCharArray()
            .let(::String)

        context.send(
            text,
            shouldTypeBeforeSend = true,
            replyToUpdate = true,
        )

        keyValueService.put(PshenitsinTolerance, context.chatKey, true, 1.minutes)
    }

    private fun isTolerant(chatId: Long): Boolean {
        return keyValueService.get(PshenitsinTolerance, ChatEasyKey(chatId), false)
    }

    private fun containsSymbolsY(text: String): Boolean {
        val splitText = text.split(spaceSplit)
        return if (splitText.first().toCharArray().isEmpty()) {
            false
        } else {
            splitText.any { word -> word.toCharArray().all { c -> c.lowercaseChar() == 'ы' } }
        }
    }
}
