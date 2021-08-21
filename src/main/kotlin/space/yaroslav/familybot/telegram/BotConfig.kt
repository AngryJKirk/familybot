package space.yaroslav.familybot.telegram

data class BotConfig(
    val botToken: String,
    val botName: String,
    val developer: String,
    val developerId: String,
    val yandexKey: String?,
    val paymentToken: String?
)
