package space.yaroslav.familybot.repos

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.repos.ifaces.PidorDictionaryRepository

@Component
class PidorDictionaryRepositoryImpl(val template: JdbcTemplate) : PidorDictionaryRepository {
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