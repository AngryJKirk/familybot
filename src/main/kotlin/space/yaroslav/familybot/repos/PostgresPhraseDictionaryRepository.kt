package space.yaroslav.familybot.repos

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.route.models.Phrase
import space.yaroslav.familybot.route.models.PhraseTheme
import java.util.concurrent.TimeUnit

@Component
class PostgresPhraseDictionaryRepository(val jdbcTemplate: JdbcTemplate) : PhraseDictionaryRepository {

    private val cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
        .build(CacheLoader.from {type: Pair<Phrase, PhraseTheme>? -> getPhrasesInternal(type)})

    override fun getPhraseTheme(chat: Chat): PhraseTheme {
        TODO()
    }

    override fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String> {
        return cache[phrase to phraseTheme]
    }

    private fun getPhrasesInternal(type: Pair<Phrase, PhraseTheme>?): List<String> {
        return jdbcTemplate.queryForList(
            "select phrase from phrase_dictionary where phrase_type_id = ? and phrase_theme_id = ?",
            String::class.java,
            type!!.first.id,
            type.second.id
        ).takeIf { it.isNotEmpty() } ?: getPhrases(type.first, PhraseTheme.DEFAULT)
    }
}
