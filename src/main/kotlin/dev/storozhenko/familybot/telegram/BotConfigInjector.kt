package dev.storozhenko.familybot.telegram

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("settings", ignoreInvalidFields = false)
data class BotConfigInjector @ConstructorBinding constructor(
    val botToken: String,
    val botName: String,
    val developer: String,
    val developerId: String,
    val botNameAliases: String?,
    val yandexKey: String?,
    val paymentToken: String?,
    val ytdlLocation: String?
)
