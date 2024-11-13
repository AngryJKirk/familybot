package dev.storozhenko.familybot.feature.ai

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

@Component
class MemoryCommandExecutor : CommandExecutor() {
    override fun command() = Command.MEMORY

    override suspend fun execute(context: ExecutorContext) {
        context.client.send(
            context,
            "Какое действие с ИИ памятью вы хотите выполнить? Эта память будет использована чтобы у бота был контекст при общении.",
            customization = {
                replyMarkup = InlineKeyboardMarkup(
                    listOf(
                        InlineKeyboardRow(
                            listOf(
                                InlineKeyboardButton("Добавить")
                                    .apply { callbackData = "add" },
                                InlineKeyboardButton("Показать что есть")
                                    .apply { callbackData = "show" },
                                InlineKeyboardButton("Стереть все")
                                    .apply { callbackData = "clear" },
                            )
                        )
                    )
                )
            })
    }
}