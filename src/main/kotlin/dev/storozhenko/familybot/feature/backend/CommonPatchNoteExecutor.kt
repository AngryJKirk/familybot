package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.core.repos.UserRepository
import org.springframework.stereotype.Component

@Component
class CommonPatchNoteExecutor(
    private val userRepository: UserRepository,
) : PatchNoteExecutor(userRepository) {

    override fun getChats() = userRepository.getChats()

    override fun getMessagePrefix() = "patch_note_common"
}