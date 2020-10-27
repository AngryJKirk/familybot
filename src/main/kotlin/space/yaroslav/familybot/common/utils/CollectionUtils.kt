package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.common.User

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
        .mapKeys { it.key.second?.replace("<", "")?.replace(">", "") }
        .map { (key, value) -> key to value.size }
        .sortedByDescending { (_, numberOfTimes) -> numberOfTimes }
        .mapIndexed { index, pair -> format(index, pair) }
}
