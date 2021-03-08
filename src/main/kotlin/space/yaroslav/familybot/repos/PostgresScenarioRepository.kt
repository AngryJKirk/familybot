package space.yaroslav.familybot.repos

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
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

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
            scenarioMoveRowMapper.mapRowNotNull(rs, rowNum),
            rs.getInt("poll_message_id")
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
            "SELECT * FROM scenario INNER JOIN scenario_move sm ON scenario.entry_move = sm.move_id",
            scenarioRowMapper
        )
    }

    override fun findMove(id: UUID): ScenarioMove? {
        return template
            .query(
                "SELECT * FROM scenario_move WHERE move_id = :id",
                mapOf("id" to id),
                scenarioMoveRowMapper
            )
            .firstOrNull()
    }

    override fun getAllCurrentGames(): Map<Chat, ScenarioMove> {
        return template.query(
            """SELECT *
        FROM (
         SELECT chat_id, scenario_move_id, MAX(state_date) AS date
         FROM scenario_states
         GROUP BY chat_id, scenario_move_id) gr
         JOIN scenario_states ss ON gr.chat_id = ss.chat_id
            AND gr.chat_id = ss.chat_id
            AND gr.date = ss.state_date
         JOIN chats c ON ss.chat_id = c.id
         JOIN scenario_move sm ON ss.scenario_move_id = sm.move_id"""
        ) { rs, rowNum ->
            rs.toChat() to (scenarioMoveRowMapper.mapRowNotNull(rs, rowNum))
        }.toMap()
    }

    override fun addState(scenarioMove: ScenarioMove, chat: Chat) {
        template.update(
            "INSERT INTO scenario_states (state_date, chat_id, scenario_move_id) VALUES (:date,:chat_id,:move_id)",
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
            SELECT * FROM scenario_states ss
            INNER JOIN scenario_move sm ON ss.scenario_move_id = sm.move_id
            WHERE chat_id = :id ORDER BY state_date DESC LIMIT 1
        """,
            mapOf("id" to chat.id),
            scenarioStateRowMapper
        ).firstOrNull()
    }

    override fun addChoice(chat: Chat, user: User, scenarioMove: ScenarioMove, chosenWay: ScenarioWay) {
        template.update(
            "INSERT INTO scenario_choices (user_id, chat_id, scenario_way_id) VALUES (:user_id, :chat_id, :scenario_way_id)",
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
           DELETE
FROM scenario_choices
WHERE chat_id = :chat_id
  AND user_id = :user_id
  AND scenario_way_id IN (:ids)
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
    SELECT * FROM scenario_choices sc
    INNER JOIN users u ON sc.user_id = u.id
    INNER JOIN scenario_way sw ON sw.way_id = sc.scenario_way_id
    WHERE chat_id = :chat_id AND scenario_way_id IN (:ids) 
    AND choice_date > :state_date
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
            "INSERT INTO scenario_poll (poll_id, chat_id, create_date, scenario_move_id, poll_message_id) VALUES (:poll_id,:chat_id,:create_date,:move_id, :poll_message_id)",
            mapOf(
                "poll_id" to scenarioPoll.pollId,
                "chat_id" to scenarioPoll.chat.id,
                "create_date" to Timestamp.from(scenarioPoll.createDate),
                "move_id" to scenarioPoll.scenarioMove.id,
                "poll_message_id" to scenarioPoll.messageId
            )
        )
    }

    override fun getDataByPollId(id: String): ScenarioPoll? {
        return template.query(
            """SELECT * FROM scenario_poll 
            INNER JOIN chats c ON scenario_poll.chat_id = c.id
            INNER JOIN scenario_move sm ON scenario_poll.scenario_move_id = sm.move_id
            WHERE poll_id = :poll_id
        """,
            mapOf(
                "poll_id" to id
            ), scenarioPollRowMapper
        ).firstOrNull()
    }

    override fun findScenarioPoll(chat: Chat, scenarioMove: ScenarioMove, afterDate: Instant): ScenarioPoll? {
        return template.query(
            """SELECT * FROM scenario_poll sp
            INNER JOIN chats c ON sp.chat_id = c.id
            INNER JOIN scenario_move sm ON sp.scenario_move_id = sm.move_id
            WHERE sp.chat_id = :chat_id AND scenario_move_id = :move_id AND create_date >= :date 
        """,
            mapOf(
                "chat_id" to chat.id,
                "move_id" to scenarioMove.id,
                "date" to Timestamp.from(afterDate)
            ),
            scenarioPollRowMapper

        ).firstOrNull()
    }

    override fun allPolls(from: Instant, to: Instant): List<ScenarioPoll> {

        return template.query(
            """SELECT * FROM scenario_poll sp
            INNER JOIN chats c ON sp.chat_id = c.id
            INNER JOIN scenario_move sm ON sp.scenario_move_id = sm.move_id                
            WHERE create_date >= :date_from AND create_date < :date_to""",
            mapOf(
                "date_from" to Timestamp.from(from),
                "date_to" to Timestamp.from(to)
            ),
            scenarioPollRowMapper
        )
    }

    override fun findMostRecentPoll(chat: Chat): ScenarioPoll? {
        return template.query(
            """SELECT * FROM scenario_poll sp
            INNER JOIN chats c ON sp.chat_id = c.id
            INNER JOIN scenario_move sm ON sp.scenario_move_id = sm.move_id
            WHERE sp.chat_id = :chat_id ORDER BY create_date DESC LIMIT 1""",
            mapOf(
                "chat_id" to chat.id
            ),
            scenarioPollRowMapper
        )
            .firstOrNull()
    }

    override fun getAllStatesOfChat(chat: Chat): List<ScenarioState> {
        return template.query(
            """
            SELECT * FROM scenario_states 
            INNER JOIN scenario_move sm ON scenario_states.scenario_move_id = sm.move_id
        """, mapOf("chat_id" to chat.id), scenarioStateRowMapper
        )
    }

    private fun findScenarioWays(moveId: UUID): List<ScenarioWay> {
        return template.query(
            """
            SELECT sw.way_id AS scenario_way_id, sm.move_id AS scenario_move_id, sw.* FROM move2way m2w  
            INNER JOIN scenario_way sw ON m2w.way_id = sw.way_id
            INNER JOIN scenario_move sm ON m2w.move_id = sm.move_id
            WHERE sm.move_id = :move_id
        """, mapOf("move_id" to moveId), scenarioWayRowMapper
        )
    }

    private fun <T> RowMapper<T>.mapRowNotNull(rs: ResultSet, rowNumber: Int): T {
        return mapRow(rs, rowNumber) ?: throw FamilyBot.InternalException("Can't find required field")
    }
}