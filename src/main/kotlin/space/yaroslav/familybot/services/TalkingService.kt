package space.yaroslav.familybot.services

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class TalkingService(private val chatLogRepository: ChatLogRepository) {

    @Timed("service.TalkingService.getReplyToUser")
    fun getReplyToUser(update: Update): String {
        return update.toUser()
            .let(this::getMessageList)
            .let(this::cleanMessages)
            .toList()
            .random()
    }

    private fun getMessageList(user: User): List<String> {
        return chatLogRepository
            .get(user)
            .takeIf { it.size > 300 }
            ?: chatLogRepository.getAll()
    }

    private fun cleanMessages(messages: List<String>): Sequence<String> {
        return messages
            .asSequence()
            .filterNot { it.split(" ").size > 10 }
            .filterNot { it.length > 600 }
            .filterNot { it.contains("http", ignoreCase = true) }
            .filterNot { it.contains("@") }
    }
}
