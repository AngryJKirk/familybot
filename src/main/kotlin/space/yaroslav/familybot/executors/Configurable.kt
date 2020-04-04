package space.yaroslav.familybot.executors

import space.yaroslav.familybot.models.FunctionId

interface Configurable {

    fun getFunctionId(): FunctionId
}
