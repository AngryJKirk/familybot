package space.yaroslav.familybot.services.dictionary

import space.yaroslav.familybot.models.Phrase

interface Dictionary {

    fun get(phrase: Phrase): String

    fun getAll(phrase: Phrase): List<String>
}
