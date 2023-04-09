package dev.storozhenko.familybot.feature.gambling

import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard

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

    override suspend fun execute(context: ExecutorContext) {
        val chatId = context.update.message.chatId.toString()

        context.sender.execute(
            SendMessage(chatId, context.phrase(Phrase.ROULETTE_MESSAGE))
                .apply {
                    replyMarkup = ForceReplyKeyboard().apply { selective = true }
                    replyToMessageId = context.update.message.messageId
                }
        )
    }

    override fun isLoggable(): Boolean {
        return false
    }
}
