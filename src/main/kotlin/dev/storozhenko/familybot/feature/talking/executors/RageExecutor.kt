package dev.storozhenko.familybot.feature.talking.executors


import dev.storozhenko.familybot.common.extensions.untilNextDay
import dev.storozhenko.familybot.core.executors.CommandExecutor
import dev.storozhenko.familybot.core.executors.Configurable
import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.core.models.dictionary.Phrase
import dev.storozhenko.familybot.core.models.telegram.Command
import dev.storozhenko.familybot.core.routers.models.ExecutorContext
import dev.storozhenko.familybot.feature.settings.models.FirstTimeInChat
import dev.storozhenko.familybot.feature.settings.models.FunctionId
import dev.storozhenko.familybot.feature.settings.models.RageMode
import dev.storozhenko.familybot.feature.settings.models.RageTolerance
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class RageExecutor(
    private val easyKeyValueService: EasyKeyValueService,
) : CommandExecutor(), Configurable {

    private val log = KotlinLogging.logger {  }

    companion object {
        const val AMOUNT_OF_RAGE_MESSAGES = 20L
    }

    override fun getFunctionId(context: ExecutorContext) = FunctionId.RAGE

    override fun command() = Command.RAGE

    override suspend fun execute(context: ExecutorContext) {
        val key = context.chatKey
        if (isRageForced(context)) {
            log.warn { "Someone forced ${command()}" }
            easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, 10.minutes)
            context.send(context.phrase(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
            return
        }

        if (isFirstLaunch(context)) {
            log.info { "First launch of ${command()} was detected, avoiding that" }
            context.send(context.phrase(Phrase.TECHNICAL_ISSUE), shouldTypeBeforeSend = true)
            return
        }

        if (isCooldown(context)) {
            log.info { "There is a cooldown of ${command()}" }
            context.send(
                context.phrase(Phrase.RAGE_DONT_CARE_ABOUT_YOU),
                shouldTypeBeforeSend = true,
            )
            return
        }
        easyKeyValueService.put(RageMode, key, AMOUNT_OF_RAGE_MESSAGES, 10.minutes)
        easyKeyValueService.put(RageTolerance, key, true, untilNextDay())
        context.send(context.phrase(Phrase.RAGE_INITIAL), shouldTypeBeforeSend = true)
    }

    private fun isCooldown(context: ExecutorContext) = easyKeyValueService.get(RageTolerance, context.chatKey, false)

    private fun isFirstLaunch(context: ExecutorContext) = easyKeyValueService.get(FirstTimeInChat, context.chatKey, false)

    private fun isRageForced(context: ExecutorContext): Boolean {
        return context.message.text.contains("FORCED" + context.user.id.toString().takeLast(4))
    }
}
