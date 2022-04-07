package space.yaroslav.familybot.services.settings

abstract class BooleanKeyType<K : EasyKey> : EasyKeyType<Boolean, K> {
    override fun getType() = Boolean::class
}

abstract class LongKeyType<K : EasyKey> : EasyKeyType<Long, K> {
    override fun getType() = Long::class
}

abstract class StringKeyType<K : EasyKey> : EasyKeyType<String, K> {
    override fun getType() = String::class
}

object FuckOffTolerance : BooleanKeyType<UserAndChatEasyKey>() {
    override fun keyType() = UserAndChatEasyKey::class
}

object RageMode : LongKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object TalkingDensity : LongKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object UkrainianLanguage : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object AskWorldDensity : StringKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object PidorStrikeStats : StringKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object Ban : StringKeyType<EasyKey>() {
    override fun keyType() = EasyKey::class
}

object CommandLimit : LongKeyType<UserAndChatEasyKey>() {
    override fun keyType() = UserAndChatEasyKey::class
}

object FirstBotInteraction : StringKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object FirstTimeInChat : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object RageTolerance : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object PidorTolerance : LongKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object BetTolerance : BooleanKeyType<UserAndChatEasyKey>() {
    override fun keyType() = UserAndChatEasyKey::class
}

object AskWorldChatUsages : LongKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object AskWorldUserUsages : LongKeyType<UserEasyKey>() {
    override fun keyType() = UserEasyKey::class
}

object FuckOffOverride : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object PshenitsinTolerance : BooleanKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}

object ProposalTo : LongKeyType<UserAndChatEasyKey>() {
    override fun keyType() = UserAndChatEasyKey::class
}

object MessageCounter : LongKeyType<UserAndChatEasyKey>() {
    override fun keyType() = UserAndChatEasyKey::class
}

object PickPidorAbilityCount : LongKeyType<UserEasyKey>() {
    override fun keyType() = UserEasyKey::class
}

object AutoPidorTimesLeft : LongKeyType<ChatEasyKey>() {
    override fun keyType() = ChatEasyKey::class
}