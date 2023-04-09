package dev.storozhenko.familybot.services.scenario.dsl

class ScenarioSqlGenerator {

    fun get(scenario: Scenario): String {
        val move = scenario.entryMove
        val scenarioInsert =
            """
            insert into scenario (scenario_id, scenario_description, scenario_name, entry_move)
            values ('${scenario.id}', '${scenario.description}', '${scenario.name}', '${scenario.entryMove.id}');
            """.trimIndent()
        return listOf(
            getForMove(move),
            getForWay(move),
            getForWayToMove(move)
        )
            .flatten()
            .distinct()
            .plus(scenarioInsert)
            .joinToString("\n")
    }

    private fun getForMove(move: Move): List<String> {
        val moveInsert =
            """insert into scenario_move (move_id, scenario_move_description, is_the_end)
           values ('${move.id}', '${move.description}', ${move.isEnd}) on conflict do nothing; 
            """.trimIndent()

        val otherMoves = move
            .ways
            .map(Way::nextMove)
            .map(this::getForMove)
            .flatten()

        return otherMoves.plus(moveInsert)
    }

    private fun getForWay(move: Move): List<String> {
        val wayInsert = move.ways.map { way ->
            """insert into scenario_way (way_id, answer_number, scenario_way_description, next_move_id)
               values ('${way.id}', ${way.answerNumber}, '${way.description}', '${way.nextMove.id}') on conflict do nothing; 
            """.trimIndent()
        }
        val otherWayInserts = move
            .ways
            .map(Way::nextMove)
            .map(this::getForWay)
            .flatten()

        return otherWayInserts.plus(wayInsert)
    }

    private fun getForWayToMove(move: Move): List<String> {
        val wayToMoveInsert =
            move.ways.map { way ->
                "insert into move2way (way_id, move_id) values ('${way.id}', '${move.id}');"
            }

        val otherWayToMoveInserts = move
            .ways
            .map(Way::nextMove)
            .map(this::getForWayToMove)
            .flatten()

        return otherWayToMoveInserts.plus(wayToMoveInsert)
    }
}
