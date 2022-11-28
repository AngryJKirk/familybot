package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.settings.BetTolerance
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UserAndChatEasyKey

@Component
class BetExecutor(
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext) = FunctionId.PIDOR

    override fun command() = Command.BET

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        if (isBetAlreadyDone(context.userAndChatKey)) {
            return {
                it.send(
                    context,
                    context.phrase(Phrase.BET_ALREADY_WAS),
                    shouldTypeBeforeSend = true
                )
            }
        }
        return {
            it.send(
                context,
                context.phrase(Phrase.BET_INITIAL_MESSAGE),
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
