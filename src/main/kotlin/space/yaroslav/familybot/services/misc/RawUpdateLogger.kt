package space.yaroslav.familybot.services.misc

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.extensions.toChat
import space.yaroslav.familybot.common.extensions.toUser
import space.yaroslav.familybot.repos.RawChatLogRepository
import java.time.Instant

@Component
class RawUpdateLogger(private val rawChatLogRepository: RawChatLogRepository) {
    private val objectMapper = ObjectMapper()

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

        val rawUpdate = objectMapper.writeValueAsString(update)
        rawChatLogRepository.add(update.toChat(), update.toUser(), text, fileId, rawUpdate, date)
    }
}
