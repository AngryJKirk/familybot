package space.yaroslav.familybot.repos

import io.micrometer.core.annotation.Timed
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import space.yaroslav.familybot.repos.ifaces.BanEntity
import space.yaroslav.familybot.repos.ifaces.BanRepository
import space.yaroslav.familybot.services.ban.Ban
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

@Component
class PostgresBanRepository(val jdbcTemplate: JdbcTemplate) : BanRepository {
    private val banRowMapper = BanRowMapper()

    @Timed("repository.BanRepository.addBan")
    override fun addBan(banEntity: BanEntity, ban: Ban) {
        jdbcTemplate.update(
            "INSERT INTO bans (ban_uuid, ban_till_date, ban_description, entity_id, entity_type_id,ban_date) VALUES (?,?,?,?,?,current_timestamp)",
            ban.banId,
            Timestamp.from(ban.till),
            ban.description,
            banEntity.entityId,
            banEntity.entityType.entityTypeId
        )
    }

    @Timed("repository.BanRepository.reduceBan")
    override fun reduceBan(ban: Ban) {
        jdbcTemplate.update("UPDATE bans set ban_till_date = current_timestamp where ban_uuid = ?", ban.banId)
    }

    @Timed("repository.BanRepository.getByEntity")
    override fun getByEntity(banEntity: BanEntity): Ban? {
        return jdbcTemplate.query(
            """SELECT * from bans where entity_id =? 
                and entity_type_id = ? 
                and ban_till_date >= current_timestamp
                and ban_date <= current_timestamp
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
