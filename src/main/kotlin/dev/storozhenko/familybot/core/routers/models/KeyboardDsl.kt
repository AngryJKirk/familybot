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

    fun row(init: RowDsl.() -> Unit) {
        val buttons = RowDsl().apply(init).buttons
        if (buttons.isNotEmpty()) {
            markup.keyboard.add(InlineKeyboardRow(buttons))
        }
    }
}

class RowDsl {
    val buttons = mutableListOf<InlineKeyboardButton>()

    fun row(init: RowDsl.() -> Unit): List<InlineKeyboardButton> {
        init.invoke(this)
        return buttons
    }

    fun button(text: String, data: () -> String) {
        buttons.add(InlineKeyboardButton(text).apply {
            callbackData = data()
        })
    }

    fun link(text: String, data: () -> String) {
        buttons.add(InlineKeyboardButton(text).apply {
            url = data()
        })
    }
}