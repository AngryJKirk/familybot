package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.dropLastDelimiter
import space.yaroslav.familybot.common.utils.random
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.KeywordRepository
import space.yaroslav.familybot.route.models.Command

@Component
class AnswerExecutor(keywordRepository: KeywordRepository) : CommandExecutor {
    override fun command(): Command {
        return Command.ANSWER
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val message = update
                .message
                .text
                .removePrefix(command().command + " ")
                .split(" или ")
                .filter { variant -> variant.isNotEmpty() }
                .takeIf { it.size >= 2 }
                ?.random()
                ?.capitalize()
                ?.dropLastDelimiter()
                ?: "Ты пидор, отъебись"
        return { it.execute(SendMessage(update.toChat().id, message)) }
    }
}

