package space.yaroslav.familybot.executors.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.bots.AbsSender
import space.yaroslav.familybot.common.extensions.send
import space.yaroslav.familybot.common.extensions.untilNextDay
import space.yaroslav.familybot.executors.Configurable
import space.yaroslav.familybot.getLogger
import space.yaroslav.familybot.models.dictionary.Phrase
import space.yaroslav.familybot.models.router.ExecutorContext
import space.yaroslav.familybot.models.router.FunctionId
import space.yaroslav.familybot.models.telegram.Command
import space.yaroslav.familybot.services.settings.EasyKeyValueService
import space.yaroslav.familybot.services.settings.FirstTimeInChat
import space.yaroslav.familybot.services.settings.RageMode
import space.yaroslav.familybot.services.settings.RageTolerance
import java.time.Duration

@Component
class RageExecutor(
    private val easyKeyValueService: EasyKeyValueService
) : CommandExecutor(), Configurable {

    private val log = getLogger()

    companion object {
        const val AMOUNT_OF_RAGE_MESSAGES = 20L
    }

    override fun getFunctionId(context: ExecutorContext): FunctionId {
        return FunctionId.RAGE
    }

    override fun command(): Command {
        return Command.RAGE
    }

    override fun execute(context: ExecutorContext): suspend (AbsSender) -> Unit {
        val key = context.chatKey
        if (isRageForced(context)) {
            log.warn("Someone forced ${command()}")
            easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
            return {
                it.send(context, context.phrase(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
            }
        }

        if (isFirstLaunch(context)) {
            log.info("First launch of ${command()} was detected, avoiding that")
            return {
                it.send(context, context.phrase(Phrase.TECHNICAL_ISSUE), shouldTypeBeforeSend = true)
            }
        }

        if (isCooldown(context)) {
            log.info("There is a cooldown of ${command()}")
            return {
                it.send(
                    context,
                    context.phrase(Phrase.RAGE_DONT_CARE_ABOUT_YOU),
                    shouldTypeBeforeSend = true
                )
            }
        }
        easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, Duration.ofMinutes(10))
        easyKeyValueService.put(RageTolerance, key, true, untilNextDay())
        return {
            it.send(context, context.phrase(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
        }
    }

    private fun isCooldown(context: ExecutorContext): Boolean {
        return easyKeyValueService.get(RageTolerance, context.chatKey, false)
    }

    private fun isFirstLaunch(context: ExecutorContext): Boolean {
        return easyKeyValueService.get(FirstTimeInChat, context.chatKey, false)
    }

    private fun isRageForced(context: ExecutorContext): Boolean {
        return context.message.text.contains(
            "FORCED" +
                    context.user.id.toString().takeLast(4)
        )
    }
}
