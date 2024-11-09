package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.feature.reactions.ReactionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated

@Component
class ReactionsRouter(
    private val userRepository: UserRepository,
    private val reactionRepository: ReactionRepository
) {
    private val log = KotlinLogging.logger { }

    fun proceed(reaction: MessageReactionUpdated) {
        val user = reaction.user
        val chat = reaction.chat
        if (chat != null && user != null) {
            userRepository.addUser(user.toUser(chat.toChat()))
            reactionRepository.add(reaction)
        } else {
            log.error { "Invalid reaction? $reaction" }
        }
    }
}