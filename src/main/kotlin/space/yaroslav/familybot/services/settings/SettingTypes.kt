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
    override fun settingType() = UserAndChatSettingsKey::class
}

object RageMode : LongSetting<ChatSettingsKey>() {
    override fun settingType() = ChatSettingsKey::class
}

object TalkingDensity : LongSetting<ChatSettingsKey>() {
    override fun settingType() = ChatSettingsKey::class
}

object UkrainianLanguage : BooleanSetting<ChatSettingsKey>() {
    override fun settingType() = ChatSettingsKey::class
}
