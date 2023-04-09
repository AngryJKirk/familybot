package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.core.models.telegram.User
import dev.storozhenko.familybot.feature.logging.repos.ChatLogRepository
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.feature.settings.models.UkrainianLanguage
import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component("Old")
class TalkingServiceOld(
    private val chatLogRepository: ChatLogRepository,
    private val translateService: TranslateService,
    private val easyKeyValueService: EasyKeyValueService
) : TalkingService {

    companion object {
        private const val minimalDatabaseSizeThreshold = 300
    }

    @Timed("service.TalkingService.getReplyToUser")
    override suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean): String {
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
