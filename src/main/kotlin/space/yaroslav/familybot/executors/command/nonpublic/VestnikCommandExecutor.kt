package space.yaroslav.familybot.executors.command.nonpublic

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.sendDeferred
import space.yaroslav.familybot.executors.command.CommandExecutor
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.repos.AskWorldRepository

@Component
class VestnikCommandExecutor(
    private val askWorldRepository: AskWorldRepository
) : CommandExecutor() {
    private val chat = Chat(id = -1001351771258L, name = null)
    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        return { sender ->
            val question = coroutineScope {
                async {
                    askWorldRepository.searchQuestion("вестник", chat).randomOrNull()?.message ?: "Выпусков нет :("
                }
            }
            sender.send(context, "Случайный выпуск Вестника Кала:")
            sender.sendDeferred(context, question, shouldTypeBeforeSend = true)
        }
    }

    override fun command() = Command.VESTNIK
}