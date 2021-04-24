package space.yaroslav.familybot.services.settings

abstract class BooleanSetting<K : SettingsKey> : EasySetting<Boolean, K> {
    override fun getType() = Boolean::class
}

abstract class LongSetting<K : SettingsKey> : EasySetting<Long, K> {
    override fun getType() = Long::class
}

abstract class StringEasySetting<K : SettingsKey> : EasySetting<String, K> {
    override fun getType() = String::class
}

object FuckOffTolerance : BooleanSetting<UserAndChatSettingsKey>() {
    override fun keyType() = UserAndChatSettingsKey::class
}

object RageMode : LongSetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object TalkingDensity : LongSetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object UkrainianLanguage : BooleanSetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object AskWorldDensity : StringEasySetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object PidorStrikeStats : StringEasySetting<ChatSettingsKey>() {
    override fun keyType() = ChatSettingsKey::class
}

object Ban : StringEasySetting<SettingsKey>() {
    override fun keyType() = SettingsKey::class
}

object CommandLimit : LongSetting<UserAndChatSettingsKey>() {
    override fun keyType() = UserAndChatSettingsKey::class
}
