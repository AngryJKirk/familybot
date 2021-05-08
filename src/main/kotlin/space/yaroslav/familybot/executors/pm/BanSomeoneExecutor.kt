package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.getMessageTokens
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.services.misc.BanService
import space.yaroslav.familybot.telegram.BotConfig

@Component
class BanSomeoneExecutor(
    private val banService: BanService,
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {

    private val banPrefix = "ban|"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {

        val command = update.getMessageTokens(delimiter = "|")
        val identification = command[1]
        val isUnban = command.getOrNull(4) == "unban"
        val chats = commonRepository.getChats()

        val chat = chats.find { it.name == identification || it.id == identification.toLongOrNull() }

        val description = command[2]
        if (chat != null) {
            return {
                if (isUnban) {
                    banService.reduceBan(chat.key())
                    it.send(update, "Unbanned chat: $chat")
                } else {
                    banService.banChat(chat, description)
                    it.send(update, "Banned chat: $chat")
                }
            }
        }

        val user = chats
            .asSequence()
            .flatMap { commonRepository.getUsers(it, activeOnly = true).asSequence() }
            .firstOrNull { it.nickname == identification || it.name == identification || it.id == identification.toLongOrNull() }

        if (user != null) {
            return {
                if (isUnban) {
                    banService.reduceBan(user.key())
                    it.send(update, "Unbanned user: $user")
                } else {
                    banService.banUser(user, description)
                    it.send(update, "Banned user: $user")
                }
            }
        }

        return {
            it.send(update, "No one found")
        }
    }

    override fun getMessagePrefix() = banPrefix
}
