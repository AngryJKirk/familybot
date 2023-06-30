package dev.storozhenko.familybot.feature.ban.executors

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.ban.services.BanService
import org.springframework.stereotype.Component

@Component
class BanSomeoneExecutor(
    private val banService: BanService,
    private val commonRepository: UserRepository,
) : OnlyBotOwnerExecutor() {

    private val banPrefix = "ban|"

    override suspend fun executeInternal(context: ExecutorContext) {
        val command = context.update.getMessageTokens(delimiter = "|")
        val identification = command[1]
        val isUnban = command.getOrNull(3) == "unban"
        val isForever = command.getOrNull(3) == "forever"
        val chats = commonRepository.getChats()

        val chat = chats.find { it.name == identification || it.id == identification.toLongOrNull() }

        val description = command[2]
        if (chat != null) {
            if (isUnban) {
                banService.removeBan(context.chatKey)
                context.sender.send(context, "Unbanned chat: $chat")
            } else {
                banService.banChat(chat, description, isForever)
                context.sender.send(context, "Banned chat: $chat")
            }
            return
        }

        val user = chats
            .asSequence()
            .flatMap { commonRepository.getUsers(it, activeOnly = true).asSequence() }
            .firstOrNull { identification.replace("@", "") in listOf(it.name, it.nickname, it.id.toString()) }

        if (user != null) {
            if (isUnban) {
                banService.removeBan(context.userKey)
                context.sender.send(context, "Unbanned user: $user")
            } else {
                banService.banUser(user, description, isForever)
                context.sender.send(context, "Banned user: $user")
            }
            return
        }

        context.sender.send(context, "No one found")
    }

    override fun getMessagePrefix() = banPrefix
}
