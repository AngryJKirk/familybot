package space.yaroslav.familybot.services.talking

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
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
        private const val minimalDatabaseSizeThreshold = 300
    }

    @Timed("service.TalkingService.getReplyToUser")
    suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean = false): String {
        val message = coroutineScope {
            async {
                val userMessages = getMessagesForUser(context.user)
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

        return if (easyKeyValueService.get(UkrainianLanguage, context.chatKey) == true) {
            translateService.translate(message.await())
        } else {
            message.await()
        }
    }

    private fun getMessagesForUser(user: User): List<String> {
        return chatLogRepository
            .get(user)
            .takeIf { messages -> messages.size > minimalDatabaseSizeThreshold }
            ?: chatLogRepository.getRandomMessagesFromCommonPool()
    }
}
