package space.yaroslav.familybot.route.executors.eventbased.keyword

import java.time.Duration
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.chatId
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.route.services.state.FuckOffState
import space.yaroslav.familybot.route.services.state.FuckOffToleranceState
import space.yaroslav.familybot.route.services.state.StateService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class FuckOffKeyWordProcessor(
    private val botConfig: BotConfig,
    private val stateService: StateService
) : KeyWordProcessor {

    private val defaultFuckOffDuration = Duration.ofMinutes(1)
    private val defaultToleranceDuration = Duration.ofHours(24)

    private val fuckOffPhrases = setOf(
        Regex(".*завали.{0,10}ебало.*"),
        Regex(".*ебало.{0,10}завали.*")
    )

    override fun canProcess(message: Message): Boolean {
        val chat = message.chat.toChat()
        return if (isReplyToBot(message) && isUserUnderTolerance(message.from.toUser(chat = chat), chat)) {
            fuckOffPhrases.any { it.matches(message.text) }
        } else {
            false
        }
    }

    override fun process(update: Update): suspend (AbsSender) -> Unit {
        val chatId = update.chatId()
        stateService.setStateForChat(chatId, FuckOffState(defaultFuckOffDuration))
        stateService.setStateForUserAndChat(update.toUser().id, chatId, FuckOffToleranceState(defaultToleranceDuration))
        return {}
    }

    private fun isReplyToBot(message: Message) =
        message.isReply && message.replyToMessage?.from?.userName == botConfig.botname

    private fun isUserUnderTolerance(user: User, chat: Chat) =
        stateService.getStateForUserAndChat(chat.id, user.id, FuckOffToleranceState::class) == null
}
