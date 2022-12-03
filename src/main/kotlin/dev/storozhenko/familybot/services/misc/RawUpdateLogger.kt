package dev.storozhenko.familybot.services.misc

import dev.storozhenko.familybot.common.extensions.toChat
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.common.extensions.toUser
import dev.storozhenko.familybot.repos.RawChatLogRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Instant

@Component
class RawUpdateLogger(private val rawChatLogRepository: RawChatLogRepository) {

    fun log(update: Update) {
        val rawMessage = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> null
        }

        if (rawMessage == null || rawMessage.from?.isBot == true) {
            return
        }

        val fileId = when {
            rawMessage.hasPhoto() -> rawMessage.photo.joinToString { it.filePath ?: it.fileId }
            rawMessage.hasDocument() -> rawMessage.document.fileId
            else -> null
        }
        val text = rawMessage.text
        val date = rawMessage
            .date
            ?.toLong()
            ?.let { Instant.ofEpochSecond(it) }
            ?: Instant.now()

        rawChatLogRepository.add(update.toChat(), update.toUser(), text, fileId, update.toJson(), date)
    }
}
