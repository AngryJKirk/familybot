package space.yaroslav.familybot.services

import io.micrometer.core.annotation.Timed
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ChatLogRepository

@Component
class TalkingService(private val chatLogRepository: ChatLogRepository) {

    companion object {
        const val cacheUpdateDelay = 1000L * 60L * 60L
        const val minimalDatabaseSizeThreshold = 300
    }

    private val log = getLogger()
    private lateinit var messages: List<String>

    init {
        updateCommonMessagesList()
    }

    @Timed("service.TalkingService.getReplyToUser")
    fun getReplyToUser(update: Update): String {
        val userMessages = getMessagesForUser(update.toUser())
            ?: messages
        return userMessages.random()
    }

    private fun getMessagesForUser(user: User): List<String>? {
        return chatLogRepository
            .get(user)
            .takeIf { it.size > minimalDatabaseSizeThreshold }
            ?.let(this::cleanMessages)
            ?.toList()
    }

    @Scheduled(fixedRate = cacheUpdateDelay, initialDelay = cacheUpdateDelay)
    fun updateCommonMessagesList() {
        messages = runCatching {
            chatLogRepository
                .getAll()
                .let(this::cleanMessages)
                .toList()
        }.getOrElse { exception ->
            log.error("Could not update message cache", exception)
            messages
        }
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
