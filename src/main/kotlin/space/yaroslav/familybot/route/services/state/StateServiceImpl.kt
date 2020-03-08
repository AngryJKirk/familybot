package space.yaroslav.familybot.route.services.state

import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

@Component
class StateServiceImpl : StateService {

    private val storage = HashMap<StateKey, MutableSet<State>>() // TODO Move that to database

    override fun setStateForChat(chatId: Long, state: State) {
        addStateToStorage(StateKey(chatId = chatId), state)
    }

    override fun setStateForUser(userId: Long, state: State) {
        addStateToStorage(StateKey(userId = userId), state)
    }

    override fun setStateForUserAndChat(userId: Long, chatId: Long, state: State) {
        addStateToStorage(StateKey(userId = userId, chatId = chatId), state)
    }

    override fun <T : State> getStateForChat(chatId: Long, requiredType: KClass<T>): T? {
        return getStateByKey(StateKey(chatId = chatId), requiredType)
    }

    override fun <T : State> getStateForUser(userId: Long, requiredType: KClass<T>): T? {
        return getStateByKey(StateKey(userId = userId), requiredType)
    }

    override fun <T : State> getStateForUserAndChat(chatId: Long, userId: Long, requiredType: KClass<T>): T? {
        return getStateByKey(StateKey(chatId = chatId, userId = userId), requiredType)
    }

    override fun <T : State> getAllStatesByChatPerUser(chatId: Long, requiredType: KClass<T>): Map<Long, T> {
        clearSpoiled()
        return storage
                .filter { it.key.chatId == chatId }
                .filter { it.key.userId != null }
                .filter { it.value::class == requiredType }
                .map { it.key.userId to getStateByKey(it.key, requiredType) }
                .mapNotNull { cleanPair(it) }
                .toMap()
    }

    override fun getFunctionToleranceStatesForChat(chatId: Long): Set<FunctionalToleranceState> {
        clearSpoiled()
        return storage.filter { it.key.chatId == chatId }
                .flatMap { it.value }
                .filterIsInstance<FunctionalToleranceState>()
                .toSet()
    }


    private fun <T> cleanPair(pair: Pair<Long?, T?>): Pair<Long, T>? {
        val first = pair.first ?: return null
        val second = pair.second ?: return null
        return first to second
    }

    private fun <T : State> getStateByKey(stateKey: StateKey, requiredType: KClass<T>): T? {
        val configurationSet = storage[stateKey] ?: return null
        val state = configurationSet.find { it::class == requiredType } ?: return null

        if (isOver(state)) {
            configurationSet.remove(state)
            return null
        }

        return requiredType.safeCast(state)
    }

    private fun addStateToStorage(stateKey: StateKey, state: State) {
        storage.computeIfAbsent(stateKey) { HashSet() }
                .add(state)
    }

    private fun clearSpoiled(){
        storage.forEach { (_, value) ->
            value.removeIf(State::checkIsItOverAlready)
        }
    }

    private fun isOver(state: State) = state.checkIsItOverAlready()


}


data class StateKey(val chatId: Long? = null, val userId: Long? = null)