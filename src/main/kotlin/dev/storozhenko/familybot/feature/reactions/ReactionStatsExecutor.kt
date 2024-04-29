package dev.storozhenko.familybot.feature.reactions

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Component
class ReactionStatsExecutor : CommandExecutor() {
    override fun command() = Command.REACTION_STATS

    override suspend fun execute(context: ExecutorContext) {
        context.sender.send(context, "За какой период реакции?", customization = {
            replyMarkup = periodsKeyboard()
        })
    }

    private fun periodsKeyboard(): InlineKeyboardMarkup {
        val buttons = listOf("день", "неделя", "месяц")
            .map { InlineKeyboardButton.builder().text(it).callbackData(it).build() }
        return InlineKeyboardMarkup(listOf(buttons))
    }
}