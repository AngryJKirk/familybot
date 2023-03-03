package dev.storozhenko.familybot.telegram

data class BotConfig(
    val botToken: String,
    val botName: String,
    val developer: String,
    val developerId: String,
    val botNameAliases: List<String>,
    val yandexKey: String?,
    val paymentToken: String?,
    val testEnvironment: Boolean,
    val ytdlLocation: String?,
    val openAiToken: String?
)
