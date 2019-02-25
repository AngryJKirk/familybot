package space.yaroslav.familybot.route.services.dictionary

import space.yaroslav.familybot.route.models.Phrase

interface Dictionary {

    fun get(phrase: Phrase): String

    fun getAll(phrase: Phrase): List<String>
}
