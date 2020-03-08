package space.yaroslav.familybot.other

import java.time.Duration
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import space.yaroslav.familybot.infrastructure.randomLong
import space.yaroslav.familybot.route.services.state.FuckOffState
import space.yaroslav.familybot.route.services.state.RageModeState
import space.yaroslav.familybot.route.services.state.StateService
import space.yaroslav.familybot.suits.FamilybotApplicationTest

class StateServiceTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var stateService: StateService

    @Test
    fun `put and get config back`() {
        val state = RageModeState(10, Duration.ofSeconds(10))
        val chatId = randomLong()
        stateService.setStateForChat(chatId, state)
        val stateFromService = stateService.getStateForChat(chatId, RageModeState::class)
        Assert.assertTrue("Should be able to get the state out of service", stateFromService != null)
    }

    @Test
    fun `put and get different config type`() {
        val state = RageModeState(10, Duration.ofSeconds(10))
        val chatId = randomLong()
        stateService.setStateForChat(chatId, state)
        val stateFromService = stateService.getStateForChat(chatId, FuckOffState::class)
        Assert.assertTrue("Should not give away any config", stateFromService == null)
    }

    @Test
    fun `should spoil config after required time`() {
        val state = RageModeState(10, Duration.ofSeconds(1))
        val chatId = randomLong()
        stateService.setStateForChat(chatId, state)
        Thread.sleep(1000)
        val stateFromService = stateService.getStateForChat(chatId, RageModeState::class)
        Assert.assertTrue("Should not return anything due to spoiling", stateFromService == null)
    }

    @Test
    fun `should remove config if it says it is over`() {
        val state = RageModeState(1, Duration.ofSeconds(10))
        val chatId = randomLong()
        stateService.setStateForChat(chatId, state)
        val stateFromService = stateService.getStateForChat(chatId, RageModeState::class)
        Assert.assertTrue("Should be able to get the state out of service", stateFromService != null)
        state.decrement()
        val stateFromServiceAfterChange = stateService.getStateForChat(chatId, RageModeState::class)
        Assert.assertTrue(
            "Should not return state again after changing the internal state",
            stateFromServiceAfterChange == null
        )
    }
}
