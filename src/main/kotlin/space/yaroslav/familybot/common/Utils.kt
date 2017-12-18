package space.yaroslav.familybot.common

import java.nio.charset.Charset
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern


fun org.telegram.telegrambots.api.objects.Chat.toChat(): Chat = Chat(this.id, this.firstName ?: "" + " " + this.lastName)

fun org.telegram.telegrambots.api.objects.User.toUser(chat: Chat? = null, telegramChat: org.telegram.telegrambots.api.objects.Chat? = null): User {
    val internalChat = telegramChat?.toChat() ?: chat
    val format = this.firstName ?: "" + " " + (this.lastName ?: "")
    return User(this.id.toLong(), internalChat!!, format, this.userName)
}

fun LocalDateTime.isToday(): Boolean {
    return LocalDate.now().atTime(0, 0).isBefore(this)
}

fun LocalDateTime.startOfYear(): LocalDateTime{
    return LocalDateTime.of(LocalDate.of(LocalDate.now().year, Month.JANUARY, 1), LocalTime.MIDNIGHT)
}

fun <T> Iterable<T>.random(): T? {
    val all = ArrayList<T>()
    iterator().forEach { all.add(it) }
    if (all.isEmpty()) {
        return null
    }
    val nextInt = ThreadLocalRandom.current().nextInt(0, all.size)
    return all[nextInt]
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

    return unicodeOutlierMatcher.replaceAll(" ")
}


fun String?.bold(): String? {
    if (this == null) return null

    return "<b>$this</b>"
}

fun String?.italic(): String? {
    if (this == null) return null

    return "<i>$this</i>"
}

fun <T> ResultSet.map(action: (ResultSet) -> T): List<T> {
    val result = ArrayList<T>()
    while (next()) {
        result.add(action.invoke(this))
    }
    return result
}

fun formatPidors(pidors: List<Pidor>): List<String> {
    fun format(index: Int, pidorStats: Pair<User, Int>): String {
        val generalName = pidorStats.first.name ?: pidorStats.first.nickname
        val i = "${index + 1}.".bold()
        val stat = "${pidorStats.second} раз(а)".italic()
        return "$i $generalName — $stat"
    }
    return pidors.groupBy { it.user }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
            .mapIndexed { index, pair -> format(index, pair) }

}
