package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
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
        .build(CacheLoader.from { type: Pair<Phrase, PhraseTheme>? -> getPhrasesInternal(type) })

    private val cacheAllPhrases = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
        .build(CacheLoader.from { type: Phrase? -> getAllPhrasesInternal(type) })

    private val themeCache = Suppliers.memoizeWithExpiration({ getThemesInternal() }, 5, TimeUnit.MINUTES)

    override fun getPhraseTheme(chat: Chat): PhraseTheme {
        TODO()
    }

    override fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String> {
        return cache[phrase to phraseTheme]
    }

    override fun getDefaultPhraseTheme(): PhraseTheme {
        return themeCache.get().first(PhraseThemeDescription::isDefault).theme
    }

    private fun getPhrasesInternal(type: Pair<Phrase, PhraseTheme>?): List<String> {
        return jdbcTemplate.queryForList(
            "select phrase from phrase_dictionary where phrase_type_id = ? and phrase_theme_id = ?",
            String::class.java,
            type!!.first.id,
            type.second.id
        ).takeIf { it.isNotEmpty() } ?: getPhrases(type.first, PhraseTheme.DEFAULT)
    }

    private fun getThemesInternal(): List<PhraseThemeDescription> {
        return jdbcTemplate.query(
            "select * from phrase_theme;"
        ) { rs, _ ->
            PhraseThemeDescription(
                PhraseTheme.values().first { it.id == rs.getInt("phrase_theme_id") },
                rs.getBoolean("active_by_default")
            )
        }
    }

    override fun getAllPhrases(phrase: Phrase): List<String> {
        return cacheAllPhrases.get(phrase)
    }

    private fun getAllPhrasesInternal(type: Phrase?): List<String> {
        return jdbcTemplate.queryForList(
            "select phrase from phrase_dictionary where phrase_type_id = ?",
            String::class.java,
            type!!.id
        )
    }
}

class PhraseThemeDescription(
    val theme: PhraseTheme,
    val isDefault: Boolean
)
