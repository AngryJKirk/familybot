package space.yaroslav.familybot.executors.pm

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.models.Priority
import space.yaroslav.familybot.repos.ifaces.CommonRepository
import space.yaroslav.familybot.services.ban.Ban
import space.yaroslav.familybot.services.ban.BanService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class BanSomeoneExecutor(
    private val botConfig: BotConfig,
    private val banService: BanService,
    private val commonRepository: CommonRepository
) :
    PrivateMessageExecutor {
    private val banPrefix = "BAN1488"
    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        val command = update.message.text.split("|")
        val identification = command[1]

        val ban = Ban(description = command[2], till = Instant.now().plus(7, ChronoUnit.DAYS))

        val chats = commonRepository.getChats()

        val chatToBan = chats.find { it.name == identification || it.id == identification.toLongOrNull() }

        if (chatToBan != null) {
            return {
                CoroutineScope(coroutineContext).launch { banService.banChat(chatToBan, ban) }
                it.send(update, "Banned chat: $chatToBan")
            }
        }

        val userToBan = chats
            .asSequence()
            .flatMap { commonRepository.getUsers(it, activeOnly = true).asSequence() }
            .firstOrNull { it.nickname == identification || it.name == identification || it.id == identification.toLongOrNull() }

        if (userToBan != null) {
            return {
                CoroutineScope(coroutineContext).launch { banService.banUser(userToBan, ban) }
                it.send(update, "Banned user: $userToBan")
            }
        }

        return {
            it.send(update, "No one found")
        }
    }

    override fun canExecute(message: Message): Boolean {
        return botConfig.developer == message.from.userName && message.text.startsWith(banPrefix)
    }

    override fun priority(update: Update) = Priority.HIGH
}
