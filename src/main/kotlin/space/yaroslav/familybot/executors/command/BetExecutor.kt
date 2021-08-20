package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.settings.BetTolerance
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig

@Component
class BetExecutor(
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    override fun getFunctionId() = FunctionId.PIDOR

    override fun command() = Command.BET

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val context = dictionary.createContext(update)
        val key = update.key()

        if (isBetAlreadyDone(key)) {
            return { it.send(update, context.get(Phrase.BET_ALREADY_WAS), shouldTypeBeforeSend = true) }
        }
        return {
            it.send(
                update,
                context.get(Phrase.BET_INITIAL_MESSAGE),
                replyToUpdate = true,
                shouldTypeBeforeSend = true,
                customization = {
                    replyMarkup = ForceReplyKeyboard
                        .builder()
                        .forceReply(true)
                        .selective(true)
                        .build()
                }
            )
        }
    }

    override fun isLoggable() = false

    private fun isBetAlreadyDone(key: UserAndChatEasyKey) =
        easyKeyValueService.get(BetTolerance, key, false)
}
