package space.yaroslav.familybot.repos

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.Chat
import space.yaroslav.familybot.common.User
import space.yaroslav.familybot.common.utils.getUuid
import space.yaroslav.familybot.common.utils.toChat
import space.yaroslav.familybot.common.utils.toUser
import space.yaroslav.familybot.repos.ifaces.ScenarioRepository
import space.yaroslav.familybot.services.scenario.Scenario
import space.yaroslav.familybot.services.scenario.ScenarioMove
import space.yaroslav.familybot.services.scenario.ScenarioPoll
import space.yaroslav.familybot.services.scenario.ScenarioState
import space.yaroslav.familybot.services.scenario.ScenarioWay
import space.yaroslav.familybot.telegram.FamilyBot

@Component
class PostgresScenarioRepository(
    jdbcTemplate: JdbcTemplate
) : ScenarioRepository {
    private val template = NamedParameterJdbcTemplate(jdbcTemplate.dataSource!!)

    private val scenarioStateRowMapper = RowMapper { rs, rowNum ->
        ScenarioState(
            scenarioMoveRowMapper.mapRowNotNull(rs, rowNum),
            rs.getTimestamp("state_date").toInstant()
        )
    }

    private val scenarioPollRowMapper = RowMapper { rs, rowNum ->
        ScenarioPoll(
            rs.getString("poll_id"),
            rs.toChat(),
            rs.getTimestamp("create_date").toInstant(),
            scenarioMoveRowMapper.mapRowNotNull(rs, rowNum)
        )
    }
    private val scenarioWayRowMapper = RowMapper { rs, _ ->
        ScenarioWay(
            rs.getUuid("scenario_way_id"),
            rs.getString("scenario_way_description"),
            rs.getInt("answer_number"),
            rs.getUuid("next_move_id")
        )
    }
    private val scenarioMoveRowMapper = RowMapper { rs, _ ->
        val moveId = rs.getUuid("move_id")
        ScenarioMove(
            moveId,
            rs.getString("scenario_move_description"),
            findScenarioWays(moveId),
            rs.getBoolean("is_the_end")
        )
    }
    private val scenarioRowMapper = RowMapper { rs, rowNum ->
        Scenario(
            rs.getUuid("scenario_id"),
            rs.getString("scenario_name"),
            rs.getString("scenario_description"),
            scenarioMoveRowMapper.mapRowNotNull(rs, rowNum)
        )
    }

    override fun getScenarios(): List<Scenario> {
        return template.query(
            "select * from scenario inner join scenario_move sm on scenario.entry_move = sm.move_id",
            scenarioRowMapper
        )
    }

    override fun findMove(id: UUID): ScenarioMove? {
        return template
            .query(
                "select * from scenario_move where move_id = :id",
                mapOf("id" to id),
                scenarioMoveRowMapper
            )
            .firstOrNull()
    }

    override fun getAllCurrentGames(): Map<Chat, ScenarioMove> {
        return template.query(
            """select *
        from (
         select chat_id, scenario_move_id, max(state_date) as date
         from scenario_states
         group by chat_id, scenario_move_id) gr
         join scenario_states ss on gr.chat_id = ss.chat_id
            and gr.chat_id = ss.chat_id
            and gr.date = ss.state_date
         join chats c on ss.chat_id = c.id
         join scenario_move sm on ss.scenario_move_id = sm.move_id"""
        ) { rs, rowNum ->
            rs.toChat() to (scenarioMoveRowMapper.mapRowNotNull(rs, rowNum))
        }.toMap()
    }

    override fun addState(scenarioMove: ScenarioMove, chat: Chat) {
        template.update(
            "insert into scenario_states (state_date, chat_id, scenario_move_id) values (:date,:chat_id,:move_id)",
            mapOf(
                "date" to Timestamp.from(Instant.now()),
                "chat_id" to chat.id,
                "move_id" to scenarioMove.id
            )
        )
    }

    override fun getState(chat: Chat): ScenarioState? {
        return template.query(
            """
            select * from scenario_states ss
            inner join scenario_move sm on ss.scenario_move_id = sm.move_id
            where chat_id = :id order by state_date desc limit 1
        """,
            mapOf("id" to chat.id),
            scenarioStateRowMapper
        ).firstOrNull()
    }

    override fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay) {
        template.update(
            "insert into scenario_choices (user_id, chat_id, scenario_way_id) values (:user_id, :chat_id, :scenario_way_id)",
            mapOf(
                "user_id" to user.id,
                "chat_id" to chat.id,
                "scenario_way_id" to chosenWay.wayId
            )
        )
    }

    override fun removeChoice(chat: Chat, user: User, scenarioMove: ScenarioMove) {
        template.update(
            """
           delete
from scenario_choices
where chat_id = :chat_id
  and user_id = :user_id
  and scenario_way_id in (:ids)
        """,
            mapOf(
                "chat_id" to chat.id,
                "user_id" to user.id,
                "ids" to scenarioMove.ways.map(ScenarioWay::wayId)
            )
        )
    }

    override fun getResultsForMove(chat: Chat, scenarioState: ScenarioState): Map<ScenarioWay, List<User>> {
        return template.query(
                """
    select * from scenario_choices sc
    inner join users u on sc.user_id = u.id
    inner join scenario_way sw on sw.way_id = sc.scenario_way_id
    where chat_id = :chat_id and scenario_way_id in (:ids) 
    and choice_date > :state_date
""",
                mapOf(
                    "chat_id" to chat.id,
                    "ids" to scenarioState.move.ways.map(ScenarioWay::wayId),
                    "state_date" to Timestamp.from(scenarioState.date)
                )
            ) { rs, rowNum -> scenarioWayRowMapper.mapRowNotNull(rs, rowNum) to rs.toUser() }
            .groupBy({ (way, _) -> way }, { (_, user) -> user })
    }

    override fun savePoll(scenarioPoll: ScenarioPoll) {
        template.update(
            "insert into scenario_poll (poll_id, chat_id, create_date, scenario_move_id) values (:poll_id,:chat_id,:create_date,:move_id)",
            mapOf(
                "poll_id" to scenarioPoll.pollId,
                "chat_id" to scenarioPoll.chat.id,
                "create_date" to Timestamp.from(scenarioPoll.createDate),
                "move_id" to scenarioPoll.scenarioMove.id
            )
        )
    }

    override fun getDataByPollId(id: String): ScenarioPoll? {
        return template.query(
            """select * from scenario_poll 
            inner join chats c on scenario_poll.chat_id = c.id
            inner join scenario_move sm on scenario_poll.scenario_move_id = sm.move_id
            where poll_id = :poll_id
        """,
            mapOf(
                "poll_id" to id
            ), scenarioPollRowMapper
        ).firstOrNull()
    }

    override fun findScenarioPoll(chat: Chat, scenarioMove: ScenarioMove, afterDate: Instant): ScenarioPoll? {
        return template.query(
            """select * from scenario_poll sp
            inner join chats c on sp.chat_id = c.id
            inner join scenario_move sm on sp.scenario_move_id = sm.move_id
            where sp.chat_id = :chat_id and scenario_move_id = :move_id and create_date >= :date 
        """,
            mapOf(
                "chat_id" to chat.id,
                "move_id" to scenarioMove.id,
                "date" to Timestamp.from(afterDate)
            ),
            scenarioPollRowMapper

        ).firstOrNull()
    }

    private fun findScenarioWays(moveId: UUID): List<ScenarioWay> {
        return template.query(
            """
            select sw.way_id as scenario_way_id, sm.move_id as scenario_move_id, sw.* from move2way m2w  
            inner join scenario_way sw on m2w.way_id = sw.way_id
            inner join scenario_move sm on m2w.move_id = sm.move_id
            where sm.move_id = :move_id
        """, mapOf("move_id" to moveId), scenarioWayRowMapper
        )
    }

    private fun <T> RowMapper<T>.mapRowNotNull(rs: ResultSet, rowNumber: Int): T {
        return mapRow(rs, rowNumber) ?: throw FamilyBot.InternalException("Can't find required field")
    }
}
