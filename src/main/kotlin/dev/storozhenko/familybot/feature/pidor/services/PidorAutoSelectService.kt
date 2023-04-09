package dev.storozhenko.familybot.feature.pidor.services

import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.feature.pidor.executors.PidorExecutor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.core.models.telegram.Chat
import dev.storozhenko.familybot.feature.settings.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.feature.settings.models.AutoPidorTimesLeft
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.feature.talking.services.Dictionary
import dev.storozhenko.familybot.telegram.BotConfig
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class PidorAutoSelectService(
    private val easyKeyValueService: EasyKeyValueService,
    private val pidorExecutor: PidorExecutor,
    private val dictionary: Dictionary,
    private val configureRepository: FunctionsConfigureRepository,
    private val botConfig: BotConfig
) {
    private val log = getLogger()

    fun autoSelect(absSender: AbsSender) {
        log.info("Running auto pidor select...")
        easyKeyValueService.getAllByPartKey(AutoPidorTimesLeft)
            .filterValues { timesLeft -> timesLeft > 0 }
            .forEach { (chatKey, timesLeft) -> runForChat(absSender, chatKey, timesLeft) }
    }

    private fun runForChat(
        absSender: AbsSender,
        chatKey: ChatEasyKey,
        timesLeft: Long
    ) {
        val chat = Chat(chatKey.chatId, name = null)
        log.info("Running auto pidor select for chat $chat")
        if (configureRepository.isEnabled(FunctionId.PIDOR, chat)) {
            val (call, wasSelected) = pidorExecutor.selectPidor(chat, chatKey, silent = true)
            if (wasSelected) {
                runBlocking {
                    call.invoke(absSender)
                    easyKeyValueService.decrement(AutoPidorTimesLeft, chatKey)
                    if (timesLeft == 1L) {
                        absSender.sendContextFree(
                            chat.idString,
                            dictionary.get(Phrase.AUTO_PIDOR_LAST_TIME, chatKey),
                            botConfig
                        )
                        easyKeyValueService.remove(AutoPidorTimesLeft, chatKey)
                    }
                }
            } else {
                log.info("Pidor was not selected for chat $chat")
            }
        } else {
            log.info("Pidor is disabled for chat $chat")
        }
    }
}
