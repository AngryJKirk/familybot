package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class GetChatListExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    private val prefix = "chats"
    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chats = commonRepository.getChats()
        return {
            it.send(update, "Active chats count=${chats.size}")
        }
    }

    override fun getMessagePrefix() = prefix
}
