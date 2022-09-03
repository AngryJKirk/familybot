package space.yaroslav.familybot.services.settings

interface BooleanKeyType<K : EasyKey> : EasyKeyType<Boolean, K> {
    override fun getType() = Boolean::class
}

interface LongKeyType<K : EasyKey> : EasyKeyType<Long, K> {
    override fun getType() = Long::class
}

interface StringKeyType<K : EasyKey> : EasyKeyType<String, K> {
    override fun getType() = String::class
}

object FuckOffTolerance : BooleanKeyType<UserAndChatEasyKey>
object RageMode : LongKeyType<ChatEasyKey>
object TalkingDensity : LongKeyType<ChatEasyKey>
object UkrainianLanguage : BooleanKeyType<ChatEasyKey>
object AskWorldDensity : StringKeyType<ChatEasyKey>
object PidorStrikeStats : StringKeyType<ChatEasyKey>
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
object TikTokDownload: BooleanKeyType<ChatEasyKey>