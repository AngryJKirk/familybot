package dev.storozhenko.familybot.core.routers

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.core.repos.ReactionRepository
import dev.storozhenko.familybot.core.repos.UserRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated

@Component
class ReactionsRouter(
    private val userRepository: UserRepository,
    private val reactionRepository: ReactionRepository) {

    fun proceed(reaction: MessageReactionUpdated) {
        userRepository.addUser(reaction.user.toUser(reaction.chat.toChat()))
        reactionRepository.add(reaction)
    }
}