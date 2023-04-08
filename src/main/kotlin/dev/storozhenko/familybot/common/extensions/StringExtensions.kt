package dev.storozhenko.familybot.common.extensions

import dev.storozhenko.familybot.models.dictionary.Pluralization
import java.util.*

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
fun String.code(): String = "<code>$this</code>"

fun pluralize(count: Int, pluralizedWordsProvider: PluralizedWordsProvider) =
    pluralize(count.toLong(), pluralizedWordsProvider)

fun pluralize(count: Long, pluralizedWordsProvider: PluralizedWordsProvider): String {
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
