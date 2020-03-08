package space.yaroslav.familybot.route.services.state

import java.time.Duration

class RageModeState(private var amountOfMessagesToBeRaged: Int, duration: Duration) : TimeLimitedState(duration) {

    fun decrement() = amountOfMessagesToBeRaged--

    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return listOf { amountOfMessagesToBeRaged <= 0 }
    }
}
