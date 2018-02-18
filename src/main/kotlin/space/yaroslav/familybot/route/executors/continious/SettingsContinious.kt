package space.yaroslav.familybot.route.executors.continious

import org.springframework.stereotype.Component
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.repos.ifaces.FunctionsConfigureRepository
import space.yaroslav.familybot.route.executors.command.SETTINGS_MESSAGE
import space.yaroslav.familybot.route.models.Command
import space.yaroslav.familybot.route.models.FunctionId
import space.yaroslav.familybot.telegram.BotConfig

@Component
class SettingsContinious(private val configureRepository: FunctionsConfigureRepository,
                         override val botConfig: BotConfig) : ContiniousConversation {


    override fun command(): Command {
        return Command.SETTINGS
    }

    override fun getDialogMessage(): String {
        return SETTINGS_MESSAGE
    }

    override fun execute(update: Update): (AbsSender) -> Unit {
        val function = FunctionId
                .values()
                .find { it.desc == update.message.text }
        if (function == null) {
            return { it.execute(SendMessage(update.message.chatId, "Опять этот пидор какую-то хуйню написал")) }
        } else {
            val chat = update.toChat()
            configureRepository.switch(function, chat)
            return {
                it.execute(SendMessage(chat.id,
                        "Ок, новый статус: ${currentState(function, chat)}")
                        .setReplyMarkup(ReplyKeyboardRemove()))
            }
        }
    }

    private fun currentState(functionId: FunctionId, chat: Chat): String {
        return when (configureRepository.isEnabled(functionId, chat)) {
            true -> "ВКЛ"
            false -> "ВЫКЛ"
        }
    }
}
