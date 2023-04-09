package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.core.routers.models.ExecutorContext

interface TalkingService {

    suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean = false): String
}
