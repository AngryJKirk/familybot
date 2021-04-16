package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import space.yaroslav.familybot.services.misc.Ban
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

@Component
class BanRepository(val jdbcTemplate: JdbcTemplate) {
    private val banRowMapper = BanRowMapper()

    @Timed("repository.BanRepository.addBan")
    fun addBan(banEntity: BanEntity, ban: Ban) {
        jdbcTemplate.update(
            "INSERT INTO bans (ban_uuid, ban_till_date, ban_description, entity_id, entity_type_id,ban_date) VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)",
            ban.banId,
            Timestamp.from(ban.till),
            ban.description,
            banEntity.entityId,
            banEntity.entityType.entityTypeId
        )
    }

    @Timed("repository.BanRepository.reduceBan")
    fun reduceBan(ban: Ban) {
        jdbcTemplate.update("UPDATE bans SET ban_till_date = CURRENT_TIMESTAMP WHERE ban_uuid = ?", ban.banId)
    }

    @Timed("repository.BanRepository.getByEntity")
    fun getByEntity(banEntity: BanEntity): Ban? {
        return jdbcTemplate.query(
            """SELECT * FROM bans WHERE entity_id =? 
                AND entity_type_id = ? 
                AND ban_till_date >= CURRENT_TIMESTAMP
                AND ban_date <= CURRENT_TIMESTAMP
                """,

            banRowMapper,
            banEntity.entityId,
            banEntity.entityType.entityTypeId
        ).firstOrNull()
    }
}

class BanRowMapper : RowMapper<Ban> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Ban {
        return Ban(
            UUID.fromString(rs.getString("ban_uuid")),
            rs.getString("ban_description"),
            rs.getTimestamp("ban_till_date").toInstant()
        )
    }
}

enum class BanEntityType(val entityTypeId: Int) {
    USER(1),
    CHAT(2)
}

data class BanEntity(val entityId: Long, val entityType: BanEntityType)

