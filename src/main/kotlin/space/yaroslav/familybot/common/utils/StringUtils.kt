package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.route.models.Command

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

fun String?.bold(): String? {
    if (this == null) return null

    return "<b>$this</b>"
}

fun String?.italic(): String? {
    if (this == null) return null

    return "<i>$this</i>"
}

fun String?.parseCommand(): Command? {
    var first = this?.split(" ")?.get(0)
    if (first?.contains("@") == true) {
        first = first.dropLastWhile { it == '@' }
    }
    return Command.values().find { it.command == first }
}

fun pluralize(count: Int, one: String, few: String, many: String): String {
    return when (Pluralization.getPlur(count)) {
        Pluralization.ONE -> one
        Pluralization.FEW -> few
        Pluralization.MANY -> many
    }
}
