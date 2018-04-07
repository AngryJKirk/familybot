package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.route.models.Command
import java.nio.charset.Charset
import java.util.regex.Pattern


fun String?.dropLastDelimiter(): String? {
    if (this.isNullOrEmpty()) {
        return this
    }
    return if (this!!.lastOrNull()?.isLetterOrDigit() != true) {
        this.dropLast(1)
    } else {
        this
    }
}

fun String?.bold(): String? {
    if (this == null) return null

    return "<b>$this</b>"
}

fun String?.italic(): String? {
    if (this == null) return null

    return "<i>$this</i>"
}

fun String?.removeEmoji(): String? {
    if (this == null) return null

    val utf8Bytes = this.toByteArray(charset(
            "UTF-8"))

    val utf8tweet = String(
            utf8Bytes, Charset.forName("UTF-8"))


    val unicodeOutliers = Pattern.compile(
            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE or Pattern.CANON_EQ or Pattern.CASE_INSENSITIVE
    )
    val unicodeOutlierMatcher = unicodeOutliers.matcher(utf8tweet)

    return unicodeOutlierMatcher.replaceAll("")
}

fun String?.parseCommand(): Command? {
    var first = this?.split(" ")?.get(0)
    if (first?.contains("@") == true) {
        first = first.dropLastWhile { it == '@' }
    }
    return Command.values().find { it.command == first }
}