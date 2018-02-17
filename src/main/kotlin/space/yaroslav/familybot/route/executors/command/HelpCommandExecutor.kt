package space.yaroslav.familybot.route.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.telegram.BotConfig

@Component
class HelpCommandExecutor(config: BotConfig) : CommandExecutor() {
    private final val message = """Запросы помощи и предложения направлять к разработчику: @${config.developer}
        Для настройки бота существует команда /settings, доступная только для админов
    """.trimMargin()

    override fun command(): Command {
        return Command.HELP
    }


    override fun execute(update: Update): (AbsSender) -> Unit {
        return {
            it.execute(SendMessage(update.toChat().id, message))
        }
    }
}