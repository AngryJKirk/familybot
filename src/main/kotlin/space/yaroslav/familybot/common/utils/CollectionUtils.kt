package space.yaroslav.familybot.common.utils

import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.telegram.FamilyBot
import java.util.concurrent.ThreadLocalRandom

fun List<User>.formatTopList(): List<String> {
    fun format(index: Int, stats: Pair<String?, Int>): String {
        val i = "${index + 1}.".bold()
        val plurWord = pluralize(stats.second, "раз", "раза", "раз")
        val stat = "${stats.second} $plurWord".italic()
        return "$i ${stats.first} — $stat"
    }
    return this.groupBy { it.id to (it.name ?: it.nickname) }
        .mapKeys { it.key.second }
        .map { it.key to it.value.size }
        .sortedByDescending { it.second }
        .mapIndexed { index, pair -> format(index, pair) }
}

fun <T> List<T>.randomNotNull(): T {
    return random() ?: throw FamilyBot.InternalException("Array should not be empty for randomNotNull() invoke")
}

fun <T> Set<T>.randomNotNull(): T {
    return random() ?: throw FamilyBot.InternalException("Array should not be empty for randomNotNull() invoke")
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
