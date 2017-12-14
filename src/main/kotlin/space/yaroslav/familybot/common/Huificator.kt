package space.yaroslav.familybot.common

import java.util.regex.Pattern

class Huificator {


    val vowels = "оеаяуюыи"
    val rules = mapOf('о' to "е", 'а' to "я", 'у' to "ю", 'ы' to "и")
    val nonLetters = Pattern.compile("[^a-я]+")
    val onlyDashes = Pattern.compile("^-*$")
    val english = Pattern.compile(".*[A-Z,a-z]+.*")

    fun huify(word: String): String? {

        if (english.matcher(word).matches()) {
            return null
        }

        var wordReplaced = word.replace(nonLetters.toRegex(), "")

        if (onlyDashes.matcher(wordReplaced).matches()) {
            return null
        }

        if (wordReplaced.startsWith("ху", true) || wordReplaced.length < 5) {
            return null
        }

        wordReplaced = wordReplaced.toLowerCase()


        val postfix = String(wordReplaced.toCharArray().dropWhile { !vowels.contains(it) }.toCharArray())

        return if (rules.containsKey(postfix[0])) {
            "ху" + rules[postfix[0]] + postfix.drop(1)
        } else {
            "ху" + postfix
        }


    }
}