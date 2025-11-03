package dev.storozhenko.familybot.feature.ai


import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class MemoryCommandExecutor : CommandExecutor() {
    override fun command() = Command.MEMORY

    override suspend fun execute(context: ExecutorContext) {
        context.send(
            "Какое действие с ИИ памятью вы хотите выполнить? Эта память будет использована чтобы у бота был контекст при общении.",
        ) {
            keyboard {
                row {
                    button("Добавить") { "add" }
                    button("Показать что есть") { "show" }
                    button("Стереть все") { "clear" }
                }
            }
        }
    }
}
