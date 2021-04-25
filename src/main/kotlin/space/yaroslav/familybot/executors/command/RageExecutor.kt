package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.utils.getLogger
import space.yaroslav.familybot.common.utils.key
import space.yaroslav.familybot.common.utils.send
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.common.utils.untilNextDay
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.models.Command
import space.yaroslav.familybot.models.FunctionId
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.FirstTimeInChat
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.settings.RageTolerance
import space.yaroslav.familybot.services.talking.Dictionary
import space.yaroslav.familybot.telegram.BotConfig
import java.time.Duration

@Component
class RageExecutor(
    private val dictionary: Dictionary,
    private val easyKeyValueService: EasyKeyValueService,
    config: BotConfig
) : CommandExecutor(config), Configurable {

    private val log = getLogger()

    companion object {
        const val AMOUNT_OF_RAGE_MESSAGES = 20L
    }

    override fun getFunctionId(): FunctionId {
        return FunctionId.RAGE
    }

    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(update: Update): suspend (AbsSender) -> Unit {
        val chat = update.toChat()
        val context = dictionary.createContext(chat)
        val key = chat.key()
        if (isRageForced(update)) {
            log.warn("Someone forced ${command()}")
            easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
            return {
                it.send(update, context.get(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
            }
        }

        if (isFirstLaunch(chat)) {
            log.info("First launch of ${command()} was detected, avoiding that")
            return {
                it.send(update, context.get(Phrase.TECHNICAL_ISSUE), shouldTypeBeforeSend = true)
            }
        }

        if (isCooldown(update)) {
            log.info("There is a cooldown of ${command()}")
            return {
                it.send(update, context.get(Phrase.RAGE_DONT_CARE_ABOUT_YOU), shouldTypeBeforeSend = true)
            }
        }
        easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
        easyKeyValueService.put(RageTolerance, key, true, untilNextDay())
        return {
            it.send(update, context.get(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
        }
    }

    private fun isCooldown(update: Update): Boolean {
        return easyKeyValueService.get(RageTolerance, update.toChat().key(), false)
    }

    private fun isFirstLaunch(chat: Chat): Boolean {
        return easyKeyValueService.get(FirstTimeInChat, chat.key(), false)
    }

    private fun isRageForced(update: Update): Boolean {
        return update.message.text.contains("FORCED" + update.toUser().id.toString().takeLast(4))
    }
}
