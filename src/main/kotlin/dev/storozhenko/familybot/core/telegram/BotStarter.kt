package dev.storozhenko.familybot.core.telegram

import dev.storozhenko.familybot.BotConfig
import dev.storozhenko.familybot.common.extensions.readTomlFromStatic
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.meta.TelegramUrl
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.tomlj.TomlParseResult

@Configuration
@Profile(BotStarter.NOT_TESTING_PROFILE_NAME)
class BotStarter {

    companion object Profile {
        const val TESTING_PROFILE_NAME = "testing"
        const val NOT_TESTING_PROFILE_NAME = "!$TESTING_PROFILE_NAME"
        private val allowedUpdates = listOf(
            "message",
            "edited_message",
            "callback_query",
            "shipping_query",
            "pre_checkout_query",
            "poll",
            "poll_answer",
            "my_chat_member",
            "chat_member",
            "message_reaction",
            "message_reaction_count"
        )
    }

    private val toml = readTomlFromStatic("commands.toml")
    private val commands: List<BotCommand> = toml
        .keySet()
        .sortedBy { key -> toml.inputPositionOf(key)?.line() }
        .map { key -> key to extractValue(toml, key) }
        .map { (command, description) -> BotCommand(command, description) }
        .toList()
    private val helpCommand: BotCommand = BotCommand("help", extractValue(toml, "help"))

    @EventListener(ApplicationReadyEvent::class)
    fun telegramBot(event: ApplicationReadyEvent) {
        val telegramBotsApi = TelegramBotsLongPollingApplication()
        val bot = event.applicationContext.getBean(FamilyBot::class.java)
        val botConfig = event.applicationContext.getBean(BotConfig::class.java)
        val telegramClient = event.applicationContext.getBean(TelegramClient::class.java)
        telegramBotsApi.registerBot(
            botConfig.botToken,
            { TelegramUrl.DEFAULT_URL },
            { i -> GetUpdates(i + 1, 100, 50, allowedUpdates) },
            bot
        )
        telegramClient.execute(
            SetMyCommands
                .builder()
                .commands(commands)
                .scope(BotCommandScopeAllGroupChats())
                .build(),
        )
        telegramClient.execute(
            SetMyCommands
                .builder()
                .command(helpCommand)
                .scope(BotCommandScopeAllPrivateChats())
                .build(),
        )
        telegramClient.execute(SendMessage(botConfig.developerId, "Bot is up"))
    }

    private fun extractValue(toml: TomlParseResult, key: String): String {
        return toml.getString(key)
            ?: throw FamilyBot.InternalException("Missing command description for key=$key")
    }
}
