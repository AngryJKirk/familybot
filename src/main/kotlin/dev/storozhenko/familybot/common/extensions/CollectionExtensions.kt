package space.yaroslav.familybot.common.extensions

import space.yaroslav.familybot.models.telegram.User

class PluralizedWordsProvider(
    val one: () -> String = { "раз" },
    val few: () -> String = { "раза" },
    val many: () -> String = { "раз" }
)

fun List<User>.formatTopList(pluralizedWordsProvider: PluralizedWordsProvider = PluralizedWordsProvider()): List<String> {
    return this
        .groupBy { (id, _, name, nickname) -> id to (name ?: nickname) }
        .mapKeys { (idToName) -> idToName.second }
        .map { (name, list) -> name to list.size }
        .sortedByDescending { (_, numberOfTimes) -> numberOfTimes }
        .mapIndexed { index, pair -> format(index, pair, pluralizedWordsProvider) }
}

fun format(index: Int, stats: Pair<String?, Int>, pluralizedWordsProvider: PluralizedWordsProvider): String {
    val (name, numberOfTimes) = stats
    val i = "${index + 1}.".bold()
    val plurWord = pluralize(numberOfTimes, pluralizedWordsProvider)
    val stat = "$numberOfTimes $plurWord".italic()
    return "$i ${name?.replace("<", "")?.replace(">", "")} — $stat"
}
