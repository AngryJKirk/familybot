package space.yaroslav.familybot.route.executors.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.randomNotNull
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.services.TextToSpeechService
import space.yaroslav.familybot.route.services.YandexSpeechType
import space.yaroslav.familybot.route.services.dictionary.Dictionary

@Component
class AnswerExecutor(val textToSpeechService: TextToSpeechService, val dictionary: Dictionary) : CommandExecutor {
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val text = update.message.text
        val message = text
            .removeRange(0, getIndexOfQuestionStart(text))
            .split(" или ")
            .filter(String::isNotEmpty)
            .takeIf(this::isOptionsCountEnough)
            ?.random()
            ?.capitalize()
            ?.dropLastDelimiter()
            ?: return {
                it.send(update, dictionary.get(Phrase.BAD_COMMAND_USAGE), replyToUpdate = true)
            }
        return {
            val textToSpeech = GlobalScope.async { textToSpeechService.toSpeech(message, emotion = randomEmotion()) }
            val sendAudio = SendVoice().apply {
                chatId = update.toChat().id.toString()
                replyToMessageId = update.message.messageId
                setVoice("Test", textToSpeech.await())
            }
            it.execute(sendAudio)
        }
    }

    private fun randomEmotion() = YandexSpeechType.values().toList().randomNotNull()

    private fun isOptionsCountEnough(options: List<String>) = options.size >= 2

    private fun getIndexOfQuestionStart(text: String) = text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0
}

