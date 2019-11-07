package space.yaroslav.familybot.route.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.RawChatLogRepository
import java.time.Instant

@Component
class RawUpdateLogger(private val rawChatLogRepository: RawChatLogRepository) {
    private final val objectMapper = ObjectMapper()

    fun log(update: Update) {
        val rawMessage = when {
            update.hasMessage() -> update.message
            update.hasEditedMessage() -> update.editedMessage
            else -> null
        }

        if (rawMessage == null || rawMessage.from?.bot == true) {
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
