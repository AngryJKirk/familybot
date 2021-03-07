package space.yaroslav.familybot.services.settings

abstract class BooleanSetting : EasySetting<Boolean> {
    override fun getType() = Boolean::class
}

abstract class LongSetting : EasySetting<Long> {
    override fun getType() = Long::class
}

abstract class StringEasySetting : EasySetting<String> {
    override fun getType() = String::class
}

object FuckOffTolerance : BooleanSetting()
object RageMode : LongSetting()
object TalkingDencity : LongSetting()