package dev.storozhenko.familybot.services.talking

import dev.storozhenko.familybot.models.router.ExecutorContext

interface TalkingService {

    suspend fun getReplyToUser(context: ExecutorContext, shouldBeQuestion: Boolean = false): String

}