package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.models.Phrase
import space.yaroslav.familybot.models.PhraseTheme
import space.yaroslav.familybot.repos.ifaces.PhraseDictionaryRepository
import space.yaroslav.familybot.telegram.FamilyBot
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class PostgresPhraseDictionaryRepository(val jdbcTemplate: JdbcTemplate) : PhraseDictionaryRepository {

    private val cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
        .build(CacheLoader.from { type: Pair<Phrase, PhraseTheme>? -> getPhrasesInternal(type) })

    private val cacheAllPhrases = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
        .build(CacheLoader.from { type: Phrase? -> getAllPhrasesInternal(type) })

    private val themeCache = Suppliers.memoizeWithExpiration({ getThemesInternal() }, 1, TimeUnit.MINUTES)

    private val themeSettingsCache =
        Suppliers.memoizeWithExpiration({ getThemesSettingsInternal() }, 1, TimeUnit.MINUTES)

    @Timed("repository.PhraseDictionaryRepository.getPhraseSettings")
    override fun getPhraseSettings(): List<PhraseThemeSetting> {
        return themeSettingsCache.get()
    }

    @Timed("repository.PhraseDictionaryRepository.getPhraseTheme")
    override fun getPhraseTheme(chat: Chat): PhraseTheme {
        TODO()
    }

    @Timed("repository.PhraseDictionaryRepository.getPhrases")
    override fun getPhrases(phrase: Phrase, phraseTheme: PhraseTheme): List<String> {
        return cache[phrase to phraseTheme]
    }

    @Timed("repository.PhraseDictionaryRepository.getDefaultPhraseTheme")
    override fun getDefaultPhraseTheme(): PhraseTheme {
        return themeCache.get().first(PhraseThemeDescription::isDefault).theme
    }

    private fun getPhrasesInternal(type: Pair<Phrase, PhraseTheme>?): List<String> {
        if (type == null) {
            throw FamilyBot.InternalException("type of phrase should not be null, seems like internal logic error")
        }
        return jdbcTemplate.queryForList(
            "SELECT phrase FROM phrase_dictionary WHERE phrase_type_id = ? AND phrase_theme_id = ?",
            String::class.java,
            type.first.id,
            type.second.id
        ).takeIf { it.isNotEmpty() } ?: getPhrases(type.first, PhraseTheme.DEFAULT)
    }

    private fun getThemesInternal(): List<PhraseThemeDescription> {
        return jdbcTemplate.query(
            "SELECT * FROM phrase_theme;"
        ) { rs, _ ->
            PhraseThemeDescription(
                PhraseTheme.values().first { it.id == rs.getInt("phrase_theme_id") },
                rs.getBoolean("active_by_default")
            )
        }
    }

    private fun getThemesSettingsInternal(): List<PhraseThemeSetting> {
        return jdbcTemplate.query(
            "SELECT * FROM phrase_theme_settings;"
        ) { rs, _ ->
            PhraseThemeSetting(
                PhraseTheme.values().first { it.id == rs.getInt("phrase_theme_id") },
                rs.getTimestamp("since").toInstant(),
                rs.getTimestamp("till").toInstant()
            )
        }
    }

    @Timed("repository.PhraseDictionaryRepository.getAllPhrases")
    override fun getAllPhrases(phrase: Phrase): List<String> {
        return cacheAllPhrases.get(phrase)
    }

    private fun getAllPhrasesInternal(type: Phrase?): List<String> {
        if (type == null) {
            throw FamilyBot.InternalException("type of phrase should not be null, seems like internal logic error")
        }
        return jdbcTemplate.queryForList(
            "SELECT phrase FROM phrase_dictionary WHERE phrase_type_id = ?",
            String::class.java,
            type.id
        )
    }
}

class PhraseThemeDescription(
    val theme: PhraseTheme,
    val isDefault: Boolean
)

class PhraseThemeSetting(
    val theme: PhraseTheme,
    val since: Instant,
    val till: Instant
)
