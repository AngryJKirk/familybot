package space.yaroslav.familybot.other

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.services.talking.DictionaryReader
import space.yaroslav.familybot.suits.FamilybotApplicationTest

class DictionaryTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var dictionaryReader: DictionaryReader

    @Test
    fun `all phrases should have at least default values`() {
        val isAllPresent = Phrase.values().all { phrase -> dictionaryReader.getAllPhrases(phrase).isNotEmpty() }
        Assertions.assertTrue(isAllPresent)
    }
}
