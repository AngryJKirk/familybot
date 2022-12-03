package dev.storozhenko.familybot.other

import dev.storozhenko.familybot.models.dictionary.Phrase
import dev.storozhenko.familybot.services.talking.DictionaryReader
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DictionaryTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var dictionaryReader: DictionaryReader

    @Test
    fun `all phrases should have at least default values`() {
        Phrase
            .values()
            .map { phrase -> phrase to dictionaryReader.getAllPhrases(phrase).isNotEmpty() }
            .forEach { (phrase, isNotEmpty) ->
                Assertions.assertTrue(
                    isNotEmpty,
                    "Phrase $phrase does not have even default value"
                )
            }
    }
}
