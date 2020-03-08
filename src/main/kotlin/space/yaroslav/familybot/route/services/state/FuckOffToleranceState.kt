package space.yaroslav.familybot.route.services.state

import java.time.Duration


class FuckOffToleranceState(duration: Duration) : TimeLimitedState(duration) {
    override fun additionalIsOverChecks(): List<() -> Boolean> {
        return emptyList()
    }

}