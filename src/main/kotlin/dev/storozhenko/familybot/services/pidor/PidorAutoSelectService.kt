package dev.storozhenko.familybot.services.pidor

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import dev.storozhenko.familybot.common.extensions.sendContextFree
import dev.storozhenko.familybot.executors.command.PidorExecutor
import dev.storozhenko.familybot.getLogger
import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.models.router.FunctionId
import dev.storozhenko.familybot.models.telegram.Chat
import dev.storozhenko.familybot.repos.FunctionsConfigureRepository
import dev.storozhenko.familybot.services.settings.AutoPidorTimesLeft
import dev.storozhenko.familybot.services.settings.ChatEasyKey
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.services.talking.Dictionary
import dev.storozhenko.familybot.telegram.BotConfig

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
