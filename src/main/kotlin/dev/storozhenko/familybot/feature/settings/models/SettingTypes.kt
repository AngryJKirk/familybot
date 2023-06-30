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
object BotOwnerPidorSkip : BooleanKeyType<ChatEasyKey>
object ChatGPTStyle : StringKeyType<ChatEasyKey>
object ChatGPTPaidTill : InstantKeyType<ChatEasyKey>
object ChatGPTFreeMessagesLeft : LongKeyType<ChatEasyKey>
object ChatGPTTokenUsageByChat : LongKeyType<ChatEasyKey>
object ChatGPTNotificationNeeded : InstantKeyType<ChatEasyKey>
object IGCookie : StringKeyType<PlainKey>

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
