package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.getMessageTokens
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.repos.CommonRepository
import space.yaroslav.familybot.telegram.BotConfig

@Component
class FindUserExecutor(
    private val commonRepository: CommonRepository,
    botConfig: BotConfig
) : OnlyBotOwnerExecutor(botConfig) {
    private val delimiter = "\n===================\n"
    override fun getMessagePrefix() = "user|"

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val tokens = update.getMessageTokens("|")
        val usersToChats = commonRepository
            .findUsersByName(tokens[1])
            .associateWith { user -> commonRepository.getChatsByUser(user) }
        return { sender ->
            sender.send(update, format(usersToChats))
        }
    }

    private fun format(userToChats: Map<User, List<Chat>>): String {

        return "Search user result:\n" + userToChats
            .map { entry -> "User: ${formatUser(entry.key)} in chats [${formatChats(entry.value)}]" }
            .joinToString(separator = delimiter)
    }

    private fun formatUser(user: User): String {
        val parts = listOfNotNull(
            "id=${user.id}",
            user.nickname?.let { nickname -> "username=$nickname" },
            user.name?.let { name -> "name=$name" })

        return "[${parts.joinToString(separator = ", ")}]"
    }

    private fun formatChats(chats: List<Chat>): String {
        return chats
            .joinToString(separator = ",\n")
            { chat: Chat -> "id=${chat.id}, chatname=${chat.name}" }
    }
}