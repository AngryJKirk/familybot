package space.yaroslav.familybot.route.services.state

import java.time.Duration
import java.time.Instant

abstract class TimeLimitedState(duration: Duration) : State {

    private val endTime = Instant.now().plusSeconds(duration.seconds)

    override fun checkIsItOverAlready(): Boolean {
        return if (Instant.now().isAfter(endTime)) {
            true
        } else {
            additionalIsOverChecks()
                .asSequence()
                .any { it() }
        }
    }

    abstract fun additionalIsOverChecks(): List<() -> Boolean>
}
