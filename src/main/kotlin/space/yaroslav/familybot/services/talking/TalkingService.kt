package space.yaroslav.familybot.services.talking

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.models.telegram.User
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ChatLogRepository
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.UkrainianLanguage

@Component
class TalkingService(
    private val chatLogRepository: ChatLogRepository,
    private val translateService: TranslateService,
    private val easyKeyValueService: EasyKeyValueService
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
    suspend fun getReplyToUser(update: Update, shouldBeQuestion: Boolean = false): String {
        val message = coroutineScope {
            async {
                val userMessages = getMessagesForUser(update.toUser())
                    ?: messages
                return@async if (shouldBeQuestion) {
                    userMessages.filter { message -> message.endsWith("?") }.randomOrNull()
                        ?: userMessages.random()
                } else {
                    userMessages.random()
                }
            }
        }

        return if (easyKeyValueService.get(UkrainianLanguage, update.toChat().key()) == true) {
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
            ?.toList()
    }

    private fun updateCommonMessagesList() {
        messages = runCatching {
            chatLogRepository
                .getAll()
        }.getOrElse { exception ->
            log.error("Could not update message cache", exception)
            messages
        }
    }
}
