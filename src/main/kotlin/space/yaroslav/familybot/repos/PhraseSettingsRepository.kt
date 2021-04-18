package space.yaroslav.familybot.repos

import com.google.common.base.Suppliers
import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.models.PhraseTheme
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class PhraseSettingsRepository(val jdbcTemplate: JdbcTemplate) {

    private val themeSettingsCache =
        Suppliers.memoizeWithExpiration({ getThemesSettingsInternal() }, 1, TimeUnit.MINUTES)

    @Timed("repository.PhraseDictionaryRepository.getPhraseSettings")
    fun getPhraseSettings(): List<PhraseThemeSetting> {
        return themeSettingsCache.get()
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
}

class PhraseThemeSetting(
    val theme: PhraseTheme,
    val since: Instant,
    val till: Instant
)
