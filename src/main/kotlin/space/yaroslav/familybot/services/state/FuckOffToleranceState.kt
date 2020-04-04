package space.yaroslav.familybot.services.state

import java.time.Duration

class FuckOffToleranceState(duration: Duration) : TimeLimitedState(duration) {
    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }
}
