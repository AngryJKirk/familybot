package space.yaroslav.familybot.common.utils

import java.util.concurrent.ThreadLocalRandom
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.telegram.FamilyBot

class PluralizedWordsProvider(
    val one: () -> String = { "раз" },
    val few: () -> String = { "раза" },
    val many: () -> String = { "раз" }
)

fun List<User>.formatTopList(pluralizedWordsProvider: PluralizedWordsProvider = PluralizedWordsProvider()): List<String> {
    fun format(index: Int, stats: Pair<String?, Int>): String {
        val (name, numberOfTimes) = stats
        val i = "${index + 1}.".bold()
        val plurWord = pluralize(numberOfTimes, pluralizedWordsProvider)
        val stat = "$numberOfTimes $plurWord".italic()
        return "$i $name — $stat"
    }
    return this.groupBy { it.id to (it.name ?: it.nickname) }
        .mapKeys { it.key.second }
        .map { (key, value) -> key to value.size }
        .sortedByDescending { (_, numberOfTimes) -> numberOfTimes }
        .mapIndexed { index, pair -> format(index, pair) }
}

fun <T> List<T>.randomNotNull(): T {
    return random() ?: throw FamilyBot.InternalException("Array should not be empty for randomNotNull() invoke")
}

fun <T> Set<T>.randomNotNull(): T {
    return random() ?: throw FamilyBot.InternalException("Array should not be empty for randomNotNull() invoke")
}

fun <T> List<T>.random(): T? {
    return when (this.size) {
        0 -> null
        1 -> this[0]
        else -> this[ThreadLocalRandom.current().nextInt(0, this.size)]
    }
}

fun <T> Set<T>.random(): T? {
    return this.toList().random()
}
