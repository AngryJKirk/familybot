package space.yaroslav.familybot.infrastructure

import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.UUID

fun SendMessage.isHtmlEnabled(): Boolean {
    val field = this::class.java
        .declaredFields
        .find { it.name == "parseMode" } ?: throw FamilyBot.InternalException("Failed to get html field")
    val isAccessibleBefore = field.isAccessible
    return (field
        .apply { isAccessible = true }
        .get(this) == ParseMode.HTML)
        .also { field.isAccessible = isAccessibleBefore }
}

fun randomUUID() = UUID.randomUUID().toString()