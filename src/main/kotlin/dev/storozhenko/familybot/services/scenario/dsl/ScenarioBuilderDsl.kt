package dev.storozhenko.familybot.services.scenario.dsl

import java.util.*

class Scenario {
    val id: UUID = UUID.randomUUID()
    lateinit var name: String
    lateinit var description: String
    lateinit var entryMove: Move

    fun init(init: Scenario.() -> Unit): Scenario {
        apply(init)
        return this
    }

    fun entry(init: Move.() -> Unit): Scenario {
        val move = Move()
        move.apply(init)
        entryMove = move
        return this
    }
}

@Suppress("unused")
class Move {

    val id: UUID = UUID.randomUUID()
    lateinit var description: String
    var ways: MutableList<Way> = mutableListOf()
    var isEnd: Boolean = false
    private var answerNumber = 0

    fun init(init: Move.() -> Unit): Move {
        apply(init)
        return this
    }

    fun end() {
        ways = mutableListOf()
        isEnd = true
    }

    fun way(wayBuilder: Way.() -> Unit): Move {
        val way = Way()
        way.answerNumber = answerNumber
        way.apply(wayBuilder)
        ways.add(way)
        answerNumber++
        return this
    }
}

class Way {

    val id: UUID = UUID.randomUUID()
    lateinit var description: String
    var answerNumber: Int? = null
    lateinit var nextMove: Move

    fun init(init: Way.() -> Unit): Way {
        apply(init)
        return this
    }

    fun nextMove(init: Move.() -> Unit): Way {
        val move = Move()
        move.apply(init)
        nextMove = move
        return this
    }
}
