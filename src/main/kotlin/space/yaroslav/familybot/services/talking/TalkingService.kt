package space.yaroslav.familybot.services.talking

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.extensions.key
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.telegram.User
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
        private const val cacheUpdateDelay = 1000L * 60L * 60L // 1 hour
        private const val minimalDatabaseSizeThreshold = 300
    }

    private val log = getLogger()
    private lateinit var commonMessages: List<String>

    init {
        updateCommonMessagesList()
    }

    @Timed("service.TalkingService.getReplyToUser")
    suspend fun getReplyToUser(executorContext: ExecutorContext, shouldBeQuestion: Boolean = false): String {
        val message = coroutineScope {
            async {
                val userMessages = getMessagesForUser(executorContext.user)
                return@async if (shouldBeQuestion) {
                    userMessages
                        .filter { message -> message.endsWith("?") }
                        .randomOrNull()
                        ?: userMessages.random()
                } else {
                    userMessages.random()
                }
            }
        }

        return if (easyKeyValueService.get(UkrainianLanguage, executorContext.chat.key()) == true) {
            translateService.translate(message.await())
        } else {
            message.await()
        }
    }

    @Scheduled(fixedRate = cacheUpdateDelay, initialDelay = cacheUpdateDelay)
    fun scheduleUpdate() {
        updateCommonMessagesList()
    }

    private fun getMessagesForUser(user: User): List<String> {
        return chatLogRepository
            .get(user)
            .takeIf { messages -> messages.size > minimalDatabaseSizeThreshold }
            ?: commonMessages
    }

    private fun updateCommonMessagesList() {
        commonMessages = runCatching { chatLogRepository.getAll() }
            .getOrElse { exception ->
                log.error("Could not update message cache", exception)
                commonMessages
            }
    }
}
