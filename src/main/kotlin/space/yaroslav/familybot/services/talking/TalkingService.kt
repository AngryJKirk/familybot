package space.yaroslav.familybot.services.talking

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ChatLogRepository
import space.yaroslav.familybot.services.dictionary.TranslateService
import space.yaroslav.familybot.services.settings.EasySettingsService
import space.yaroslav.familybot.services.settings.UkrainianLanguage

@Component
class TalkingService(
    private val chatLogRepository: ChatLogRepository,
    private val translateService: TranslateService,
    private val easySettingsService: EasySettingsService
) {

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
    suspend fun getReplyToUser(update: Update): String {
        val message = coroutineScope {
            async {
                val userMessages = getMessagesForUser(update.toUser())
                    ?: messages
                return@async userMessages.random()
            }
        }

        return if (easySettingsService.get(UkrainianLanguage, update.toChat().key()) == true) {
            translateService.translate(message.await())
        } else {
            message.await()
        }
    }

    @Scheduled(fixedRate = cacheUpdateDelay, initialDelay = cacheUpdateDelay)
    fun scheduleUpdate() {
        updateCommonMessagesList()
    }

    private fun getMessagesForUser(user: User): List<String>? {
        return chatLogRepository
            .get(user)
            .takeIf { it.size > minimalDatabaseSizeThreshold }
            ?.let(this::cleanMessages)
            ?.toList()
    }

    private fun updateCommonMessagesList() {
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
            .filterNot { it.contains("сучар", ignoreCase = true) }
            .filterNot { it.contains("@") }
    }
}
