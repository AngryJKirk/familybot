package space.yaroslav.familybot.common.utils

import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.User
import java.util.concurrent.ThreadLocalRandom


fun List<User>.formatTopList(): List<String> {
    fun format(index: Int, stats: Pair<User, Int>): String {
        val generalName = stats.first.name ?: stats.first.nickname
        val i = "${index + 1}.".bold()
        val stat = "${stats.second} раз(а)".italic()
        return "$i $generalName — $stat"
    }
    return this.groupBy { it }
            .map { it.key to it.value.size }
            .sortedByDescending { it.second }
            .mapIndexed { index, pair -> format(index, pair) }
}

fun <T> List<T>.random(): T? {
    if (this.isEmpty()) {
        return null
    }
    if (this.size == 1) {
        return this[0]
    }
    return this[ThreadLocalRandom.current().nextInt(0, this.size)]
}

fun <T> Set<T>.random(): T? {
    return this.toList().random()
}

fun List<Pair<String, String>>.toInlineKeyBoard(): List<List<InlineKeyboardButton>> {
    return this
            .map { InlineKeyboardButton("${it.first} ${it.second}").setCallbackData(it.first) }
            .chunked(2)
}