package dev.storozhenko.familybot.feature.backend

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.feature.settings.models.ChatGPTPaidTill
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class GptPatchNoteExecutor(
    private val easyKeyValueService: EasyKeyValueService,
    userRepository: UserRepository
) : PatchNoteExecutor(userRepository) {

    override fun getChats(): List<Chat> {
        return easyKeyValueService
            .getAllByPartKey(ChatGPTPaidTill)
            .filter { (_, paidTill) -> paidTill.isAfter(Instant.now()) }
            .map { (key, _) -> Chat(key.chatId, name = null) }
            .toList()
    }

    override fun getMessagePrefix() = "patch_note_gpt"
}