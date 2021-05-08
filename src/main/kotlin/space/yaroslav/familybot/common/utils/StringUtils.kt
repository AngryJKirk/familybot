package space.yaroslav.familybot.common.utils

import space.yaroslav.familybot.models.dictionary.Pluralization
import java.util.Locale

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

fun pluralize(count: Int, pluralizedWordsProvider: PluralizedWordsProvider): String {
    return when (Pluralization.getPlur(count)) {
        Pluralization.ONE -> pluralizedWordsProvider.one()
        Pluralization.FEW -> pluralizedWordsProvider.few()
        Pluralization.MANY -> pluralizedWordsProvider.many()
    }
}

fun String.capitalized(): String {
    return this.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase(Locale.getDefault())
        } else {
            char.toString()
        }
    }
}

