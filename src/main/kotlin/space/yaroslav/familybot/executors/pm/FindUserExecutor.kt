package space.yaroslav.familybot.executors.pm

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.models.telegram.User
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
            .distinctBy(User::id)
            .associateWith { user -> commonRepository.getChatsByUser(user) }
        return { sender ->
            if (usersToChats.isEmpty()) {
                sender.send(update, "No one found, master")
            } else {
                usersToChats.toList().chunked(5).forEach { chunk ->
                    sender.send(update, format(chunk))
                }
            }
        }
    }

    private fun format(userToChats: List<Pair<User, List<Chat>>>): String {

        return "Search user result:\n" + userToChats.joinToString(separator = delimiter) { pair ->
            "User: ${formatUser(pair.first)} in chats [${formatChats(pair.second)}]"
        }
    }

    private fun formatUser(user: User): String {
        val parts = listOfNotNull(
            "id=${user.id}",
            user.nickname?.let { nickname -> "username=$nickname" },
            user.name?.let { name -> "name=$name" }
        )

        return "[${parts.joinToString(separator = ", ")}]"
    }

    private fun formatChats(chats: List<Chat>): String {
        return chats
            .joinToString(separator = ",\n") { chat: Chat -> "id=${chat.id}, chatname=${chat.name}" }
    }
}
