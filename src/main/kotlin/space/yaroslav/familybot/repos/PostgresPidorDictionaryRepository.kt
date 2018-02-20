package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Pluralization
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository

@Component
class PostgresPidorDictionaryRepository(val template: JdbcTemplate) : PidorDictionaryRepository {
    override fun getLeaderBoardPhrase(pluralization: Pluralization): List<String> {
        return template.query("SELECT * FROM pidor_leaderboard_dictionary_v2 WHERE plur_id = ${pluralization.code}",
                { rs, _ -> rs.getString("message") })
    }

    override fun getStart(): List<String> {
        return template.queryForList("SELECT * FROM pidor_dictionary_start", String::class.java)
    }

    override fun getMiddle(): List<String> {
        return template.queryForList("SELECT * FROM pidor_dictionary_middle", String::class.java)
    }

    override fun getFinish(): List<String> {
        return template.queryForList("SELECT * FROM pidor_dictionary_finisher", String::class.java)
    }
}