package dev.storozhenko.familybot.feature.settings.models

import dev.storozhenko.familybot.common.extensions.parseJson
import dev.storozhenko.familybot.common.extensions.toJson
import dev.storozhenko.familybot.core.keyvalue.models.BooleanKeyType
import dev.storozhenko.familybot.core.keyvalue.models.ChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKey
import dev.storozhenko.familybot.core.keyvalue.models.EasyKeyType
import dev.storozhenko.familybot.core.keyvalue.models.InstantKeyType
import dev.storozhenko.familybot.core.keyvalue.models.LongKeyType
import dev.storozhenko.familybot.core.keyvalue.models.PlainKey
import dev.storozhenko.familybot.core.keyvalue.models.StringKeyType
import dev.storozhenko.familybot.core.keyvalue.models.UserAndChatEasyKey
import dev.storozhenko.familybot.core.keyvalue.models.UserEasyKey
import dev.storozhenko.familybot.feature.pidor.models.PidorStrikes
import dev.storozhenko.familybot.feature.story.PollResults
import dev.storozhenko.familybot.feature.story.StoryMessages
import java.time.Instant

object FuckOffTolerance : BooleanKeyType<UserAndChatEasyKey>
object RageMode : LongKeyType<ChatEasyKey>
object TalkingDensity : LongKeyType<ChatEasyKey>
object UkrainianLanguage : BooleanKeyType<ChatEasyKey>
object AskWorldDensity : StringKeyType<ChatEasyKey>
object Ban : StringKeyType<EasyKey>
object CommandLimit : LongKeyType<UserAndChatEasyKey>
object FirstBotInteraction : StringKeyType<ChatEasyKey>
object FirstTimeInChat : BooleanKeyType<ChatEasyKey>
object RageTolerance : BooleanKeyType<ChatEasyKey>
object PidorTolerance : LongKeyType<ChatEasyKey>
object BetTolerance : BooleanKeyType<UserAndChatEasyKey>
object AskWorldChatUsages : LongKeyType<ChatEasyKey>
object AskWorldUserUsages : LongKeyType<UserEasyKey>
object FuckOffOverride : BooleanKeyType<ChatEasyKey>
object PshenitsinTolerance : BooleanKeyType<ChatEasyKey>
object ProposalTo : LongKeyType<UserAndChatEasyKey>
object MessageCounter : LongKeyType<UserAndChatEasyKey>
object PickPidorAbilityCount : LongKeyType<UserEasyKey>
object AutoPidorTimesLeft : LongKeyType<ChatEasyKey>
object TikTokDownload : BooleanKeyType<ChatEasyKey>
object TwitterUnroll : BooleanKeyType<ChatEasyKey>
object BotOwnerPidorSkip : BooleanKeyType<ChatEasyKey>
object ChatGPTStyle : StringKeyType<ChatEasyKey>
object ChatGPTPaidTill : InstantKeyType<ChatEasyKey>
object ChatGPTFreeMessagesLeft : LongKeyType<ChatEasyKey>
object ChatGPTTokenUsageByChat : LongKeyType<ChatEasyKey>
object ChatGPTNotificationNeeded : InstantKeyType<ChatEasyKey>
object ChatGPTSummaryCooldown : BooleanKeyType<ChatEasyKey>
object ChatGPTReactionsCooldown : BooleanKeyType<ChatEasyKey>
object ChatGPT4Enabled : BooleanKeyType<ChatEasyKey>
object ChatGPTTalkingDisabled : BooleanKeyType<ChatEasyKey>
object ChatGPTMemory: StringKeyType<ChatEasyKey>
object IGCookie : StringKeyType<PlainKey>
object PaymentKey : StringKeyType<PlainKey>
object RefundNeedsToPressTime : LongKeyType<PlainKey>
object AdCooldown : InstantKeyType<ChatEasyKey>
object RagContext: BooleanKeyType<ChatEasyKey>

object StoryGameActive : BooleanKeyType<ChatEasyKey>
object StoryPollBlocked : InstantKeyType<ChatEasyKey>
object StoryPollsCounter : LongKeyType<ChatEasyKey>
object StoryContext : EasyKeyType<StoryMessages, ChatEasyKey> {
    override fun mapToString(value: StoryMessages) = value.toJson()

    override fun mapFromString(value: String) = value.parseJson<StoryMessages>()

}

object StoryCurrentPollResults : EasyKeyType<PollResults, ChatEasyKey> {
    override fun mapToString(value: PollResults) = value.toJson()

    override fun mapFromString(value: String) = value.parseJson<PollResults>()
}


object PidorStrikeStats : EasyKeyType<PidorStrikes, ChatEasyKey> {
    override fun mapToString(value: PidorStrikes) = value.toJson()

    override fun mapFromString(value: String): PidorStrikes {
        return if (value.isBlank()) {
            PidorStrikes()
        } else {
            value.parseJson()
        }
    }
}

object AskWorldIgnore : EasyKeyType<Map<String, Instant>, ChatEasyKey> {
    override fun mapToString(value: Map<String, Instant>): String {
        return value
            .map { it.key to it.value.epochSecond }
            .toMap()
            .toJson()
    }

    override fun mapFromString(value: String): Map<String, Instant> {
        return value.parseJson<Map<String, Long>>()
            .map { it.key to Instant.ofEpochSecond(it.value) }
            .toMap()
    }

}
