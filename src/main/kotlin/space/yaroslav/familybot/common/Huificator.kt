package space.yaroslav.familybot.common

import java.util.regex.Pattern

object Huificator {


    private val vowels = "ёэоеаяуюыи"
    private val rules = mapOf('о' to "е", 'а' to "я", 'у' to "ю", 'ы' to "и", 'э' to "е")
    private val nonLetters = Pattern.compile("[^a-я]+")
    private val onlyDashes = Pattern.compile("^-*$")
    private val english = Pattern.compile(".*[A-Z,a-z]+.*")

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
            "ху$postfix"
        }


    }
}