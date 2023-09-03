package dev.storozhenko.familybot.common.extensions

import dev.storozhenko.familybot.core.models.telegram.User

class PluralizedWordsProvider(
    val one: () -> String = { "раз" },
    val few: () -> String = { "раза" },
    val many: () -> String = { "раз" },
)

@Suppress("ConvertCallChainIntoSequence")
fun List<User>.formatTopList(pluralizedWordsProvider: PluralizedWordsProvider = PluralizedWordsProvider()): List<String> {
    return this
        .groupBy { (id, _, name, nickname) -> id to (name ?: nickname) }
        .toList()
        .map { (idToName, list) -> idToName.second to list }
        .map { (name, list) -> name to list.size }
        .sortedByDescending { (_, numberOfTimes) -> numberOfTimes }
        .mapIndexed { index, pair -> format(index, pair, pluralizedWordsProvider) }
        .toList()
}

private fun format(index: Int, stats: Pair<String?, Int>, pluralizedWordsProvider: PluralizedWordsProvider): String {
    val (name, numberOfTimes) = stats
    val i = "${index + 1}.".bold()
    val plurWord = pluralize(numberOfTimes, pluralizedWordsProvider)
    val stat = "$numberOfTimes $plurWord".italic()
    return "$i ${name?.replace("<", "")?.replace(">", "")} — $stat"
}
