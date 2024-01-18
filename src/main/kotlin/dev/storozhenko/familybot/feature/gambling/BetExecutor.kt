package dev.storozhenko.familybot.feature.gambling

import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.BetTolerance
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard

@Component
class BetExecutor(
    private val easyKeyValueService: EasyKeyValueService,
) : CommandExecutor(), Configurable {

    override fun getFunctionId(context: ExecutorContext) = FunctionId.PIDOR

    override fun command() = Command.BET

    override suspend fun execute(context: ExecutorContext) {
        if (isBetAlreadyDone(context.userAndChatKey)) {
            context.sender.send(
                context,
                context.phrase(Phrase.BET_ALREADY_WAS),
                shouldTypeBeforeSend = true,
            )
            return
        }
        context.sender.send(
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
            },
        )
    }

    override fun isLoggable() = false

    private fun isBetAlreadyDone(key: UserAndChatEasyKey) = easyKeyValueService.get(BetTolerance, key, false)
}
