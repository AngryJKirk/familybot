package space.yaroslav.familybot.services.pidor

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.extensions.sendContextFree
import space.yaroslav.familybot.executors.command.PidorExecutor
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Chat
import space.yaroslav.familybot.repos.FunctionsConfigureRepository
import space.yaroslav.familybot.services.settings.AutoPidorTimesLeft
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class PidorAutoSelectService(
    private val easyKeyValueService: EasyKeyValueService,
    private val pidorExecutor: PidorExecutor,
    private val familyBot: FamilyBot,
    private val dictionary: Dictionary,
    private val configureRepository: FunctionsConfigureRepository
) {
    private val log = getLogger()

    @Scheduled(cron = "0 0 10 * * *")
    fun autoSelect() {
        log.info("Running auto pidor select...")
        easyKeyValueService.getAllByPartKey(AutoPidorTimesLeft)
            .filterValues { timesLeft -> timesLeft > 0 }
            .forEach { (chatKey, timesLeft) ->
                val chat = Chat(chatKey.chatId, name = null)
                log.info("Running auto pidor select for chat $chat")
                if (configureRepository.isEnabled(FunctionId.PIDOR, chat)) {
                    val (call, wasSelected) = pidorExecutor.selectPidor(chat, chatKey, silent = true)
                    if (wasSelected) {
                        runBlocking {
                            call.invoke(familyBot)
                            easyKeyValueService.decrement(AutoPidorTimesLeft, chatKey)
                            if (timesLeft == 1L) {
                                familyBot.sendContextFree(
                                    chat.idString,
                                    dictionary.get(Phrase.AUTO_PIDOR_LAST_TIME, chatKey),
                                    familyBot.config
                                )
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
}
