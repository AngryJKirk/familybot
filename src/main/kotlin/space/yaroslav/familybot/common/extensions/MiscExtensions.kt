package space.yaroslav.familybot.common.extensions

import java.util.concurrent.ThreadLocalRandom

fun randomInt(from: Int, to: Int) = ThreadLocalRandom.current().nextInt(from, to)

fun randomBoolean(probability: Long? = null): Boolean {
    return if (probability == null) {
        ThreadLocalRandom.current().nextBoolean()
    } else {
        ThreadLocalRandom.current().nextLong(0, probability) == 0L
    }
}
