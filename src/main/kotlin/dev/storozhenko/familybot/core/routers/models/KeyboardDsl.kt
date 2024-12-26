package dev.storozhenko.familybot.core.routers.models

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

class KeyboardDsl {

    private val markup = InlineKeyboardMarkup(mutableListOf())
    fun keyboard(init: KeyboardDsl.() -> Unit): InlineKeyboardMarkup {
        init.invoke(this)
        return markup
    }

    fun row(vararg buttons: InlineKeyboardButton) = row(buttons.toList())

    fun row(buttons: List<InlineKeyboardButton>) {
        markup.keyboard.add(InlineKeyboardRow(buttons))
    }

    fun button(text: String, data: () -> String): InlineKeyboardButton {
        return InlineKeyboardButton(text).apply {
            callbackData = data()
        }
    }

    fun link(text: String, data: () -> String): InlineKeyboardButton {
        return InlineKeyboardButton(text).apply {
            url = data()
        }
    }

}
