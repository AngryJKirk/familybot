package space.yaroslav.familybot.repos.ifaces

import space.yaroslav.familybot.common.Pluralization

interface PidorDictionaryRepository {

    fun getStart(): List<String>

    fun getMiddle(): List<String>

    fun getFinish(): List<String>

    fun getLeaderBoardPhrase(pluralization: Pluralization): List<String>
}