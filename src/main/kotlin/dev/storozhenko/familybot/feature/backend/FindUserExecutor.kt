package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.common.extensions.getMessageTokens
import dev.storozhenko.familybot.common.extensions.send
import dev.storozhenko.familybot.core.executors.OnlyBotOwnerExecutor
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import org.springframework.stereotype.Component

@Component
class FindUserExecutor(
    private val commonRepository: UserRepository,
) : OnlyBotOwnerExecutor() {
    private val delimiter = "\n===================\n"
    override fun getMessagePrefix() = "user|"

    override suspend fun executeInternal(context: ExecutorContext) {
        val tokens = context.update.getMessageTokens("|")
        val usersToChats = commonRepository
            .findUsersByName(tokens[1])
            .distinctBy(User::id)
            .associateWith { user -> commonRepository.getChatsByUser(user) }
        if (usersToChats.isEmpty()) {
            context.sender.send(context, "No one found, master")
        } else {
            usersToChats.toList().chunked(5).forEach { chunk ->
                context.sender.send(context, format(chunk))
            }
        }
    }

    private fun format(userToChats: List<Pair<User, List<Chat>>>): String {
        return "Search user result:\n" +
                userToChats
                    .joinToString(separator = delimiter) { (user, chats) ->
                        "User: ${formatUser(user)} in chats [${formatChats(chats)}]"
                    }
    }

    private fun formatUser(user: User): String {
        val parts = listOfNotNull(
            "id=${user.id}",
            user.nickname?.let { nickname -> "username=$nickname" },
            user.name?.let { name -> "name=$name" },
        )

        return "[${parts.joinToString(separator = ", ")}]"
    }

    private fun formatChats(chats: List<Chat>): String {
        return chats
            .joinToString(separator = ",\n") { (id, name): Chat -> "id=$id, chatname=$name" }
    }
}
