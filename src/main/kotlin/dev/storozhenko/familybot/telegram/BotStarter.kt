package dev.storozhenko.familybot.telegram

import dev.storozhenko.familybot.common.extensions.readTomlFromStatic
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.tomlj.TomlParseResult

@Configuration
@Profile(BotStarter.NOT_TESTING_PROFILE_NAME)
class BotStarter {

    companion object Profile {
        const val TESTING_PROFILE_NAME = "testing"
        const val NOT_TESTING_PROFILE_NAME = "!$TESTING_PROFILE_NAME"
    }

    private val commands: List<BotCommand>
    private val helpCommand: BotCommand

    init {
        val toml = readTomlFromStatic("commands.toml")

        helpCommand = BotCommand("help", extractValue(toml, "help"))
        commands = toml
            .keySet()
            .sortedBy { key -> toml.inputPositionOf(key)?.line() }
            .map { key -> key to extractValue(toml, key) }
            .map { (command, description) -> BotCommand(command, description) }
            .toList()
    }

    @EventListener(ApplicationReadyEvent::class)
    fun telegramBot(event: ApplicationReadyEvent) {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        val bot = event.applicationContext.getBean(FamilyBot::class.java)
        telegramBotsApi.registerBot(bot)
        bot.execute(
            SetMyCommands
                .builder()
                .commands(commands)
                .scope(BotCommandScopeAllGroupChats())
                .build()
        )
        bot.execute(
            SetMyCommands
                .builder()
                .command(helpCommand)
                .scope(BotCommandScopeAllPrivateChats())
                .build()
        )
    }

    @Bean
    fun telegramDownloader(botConfig: BotConfig): TelegramFileDownloader {
        return TelegramFileDownloader { botConfig.botToken }
    }

    private fun extractValue(toml: TomlParseResult, key: String): String {
        return toml.getString(key)
            ?: throw FamilyBot.InternalException("Missing command description for key=$key")
    }
}
