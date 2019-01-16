package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.PhraseTheme

@Component
class PostgresPhraseDictionaryRepository(val jdbcTemplate: JdbcTemplate) : PhraseDictionaryRepository {
    override fun getPhraseTheme(chat: Chat): PhraseTheme {
        TODO()
    }

    override fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String> {
        return jdbcTemplate.queryForList(
            "select phrase from phrase_dictionary where phrase_type_id = ? and phrase_theme_id = ?",
            String::class.java,
            phrase,
            phraseTheme
        ).takeIf { it.isNotEmpty() } ?: getPhrases(phrase, PhraseTheme.DEFAULT)
    }
}
