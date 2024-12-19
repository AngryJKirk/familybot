package dev.storozhenko.familybot.core.telegram

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.repos.UserRepository
import dev.storozhenko.familybot.feature.pidor.repos.PidorRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MigrateService(
    private val pidorRepository: PidorRepository,
    private val easyKeyValueService: EasyKeyValueService,
    private val userRepository: UserRepository,
) {
    private val log = KotlinLogging.logger {}

    fun migrate(from: Long, to: Long) {
        try {
            val fromChat = userRepository.getChatsAll().firstOrNull { it.id == from } ?: return
            val toChat = fromChat.copy(id = to)
            userRepository.addChat(toChat)
            pidorRepository.migrate(fromChat, toChat)
            easyKeyValueService.migrate(fromChat, toChat)
            log.info { "Migrated from $from to $to" }
        } catch (e: Exception) {
            log.error(e) { "Could not migrate from $from to $to" }
        }

    }
}