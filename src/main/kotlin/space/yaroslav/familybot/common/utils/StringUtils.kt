package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.models.Command

fun String?.dropLastDelimiter(): String? {
    if (this.isNullOrEmpty()) {
        return this
    }
    return if (this.lastOrNull()?.isLetterOrDigit() != true) {
        this.dropLast(1)
    } else {
        this
    }
}

fun String?.boldNullable(): String? {
    if (this == null) return null

    return "<b>$this</b>"
}

fun String.bold(): String = "<b>$this</b>"

fun String.italic(): String = "<i>$this</i>"

fun String?.parseCommand(): Command? {
    var first = this?.split(" ")?.get(0)
    if (first?.contains("@") == true) {
        first = first.dropLastWhile { it == '@' }
    }
    return Command.values().find { it.command == first }
}

fun pluralize(count: Int, pluralizedWordsProvider: PluralizedWordsProvider): String {
    return when (Pluralization.getPlur(count)) {
        Pluralization.ONE -> pluralizedWordsProvider.one()
        Pluralization.FEW -> pluralizedWordsProvider.few()
        Pluralization.MANY -> pluralizedWordsProvider.many()
    }
}
