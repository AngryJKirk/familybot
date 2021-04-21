package space.yaroslav.familybot.executors.pm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.BotConfig
import kotlin.coroutines.coroutineContext

@Component
class BanSomeoneExecutor(
    private val banService: BanService,
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    private val banPrefix = "BAN1488"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        val command = update.message.text.split("|")
        val identification = command[1]

        val chats = commonRepository.getChats()

        val chatToBan = chats.find { it.name == identification || it.id == identification.toLongOrNull() }

        val description = command[2]
        if (chatToBan != null) {
            return {
                CoroutineScope(coroutineContext).launch { banService.banChat(chatToBan, description) }
                it.send(update, "Banned chat: $chatToBan")
            }
        }

        val userToBan = chats
            .asSequence()
            .flatMap { commonRepository.getUsers(it, activeOnly = true).asSequence() }
            .firstOrNull { it.nickname == identification || it.name == identification || it.id == identification.toLongOrNull() }

        if (userToBan != null) {
            return {
                CoroutineScope(coroutineContext).launch { banService.banUser(userToBan, description) }
                it.send(update, "Banned user: $userToBan")
            }
        }

        return {
            it.send(update, "No one found")
        }
    }

    override fun getMessagePrefix() = banPrefix
}
