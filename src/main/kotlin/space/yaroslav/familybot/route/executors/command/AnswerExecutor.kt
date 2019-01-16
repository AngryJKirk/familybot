package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.methods.send.SendVoice
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.random
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

    override fun execute(update: Update): (AbsSender) -> Unit {
        val text = update.message.text
        val message = text
            .removeRange(0, text.indexOfFirst { it == ' ' }.takeIf { it >= 0 } ?: 0)
            .split(" или ")
            .filter { variant -> variant.isNotEmpty() }
            .takeIf { it.size >= 2 }
            ?.random()
            ?.capitalize()
            ?.dropLastDelimiter()
            ?: return {
                it.execute(
                    SendMessage(update.toChat().id, dictionary.get(Phrase.BAD_COMMAND_USAGE))
                        .setReplyToMessageId(update.message.messageId)
                )
            }
        return {
            val sendAudio = SendVoice()
            sendAudio.chatId = update.toChat().id.toString()
            val emotion = YandexSpeechType.values().toList().random()!!
            sendAudio.setNewVoice("Test", textToSpeechService.toSpeech(message, emotion = emotion))
            sendAudio.replyToMessageId = update.message.messageId
            it.sendVoice(sendAudio)
        }
    }
}

