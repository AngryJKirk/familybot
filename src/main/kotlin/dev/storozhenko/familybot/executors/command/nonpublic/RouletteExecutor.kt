package dev.storozhenko.familybot.executors.command.nonpublic

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.executors.Configurable
import dev.storozhenko.familybot.executors.command.CommandExecutor
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.ExecutorContext
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Command

const val ROULETTE_MESSAGE = "Выбери число от 1 до 6"

@Component
@Deprecated(message = "Replaced with BetExecutor")
class RouletteExecutor : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.PIDOR
    }

    override fun command(): Command {
        return Command.ROULETTE
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {

        val chatId = context.update.message.chatId.toString()

        return {
            it.execute(
                SendMessage(chatId, context.phrase(Phrase.ROULETTE_MESSAGE))
                    .apply {
                        replyMarkup = ForceReplyKeyboard().apply { selective = true }
                        replyToMessageId = context.update.message.messageId
                    }
            )
        }
    }

    override fun isLoggable(): Boolean {
        return false
    }
}
