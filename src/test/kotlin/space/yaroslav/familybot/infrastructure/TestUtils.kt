package space.yaroslav.familybot.infrastructure

import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

fun randomUUID() = UUID.randomUUID().toString()
fun randomInt() = ThreadLocalRandom.current().nextInt()
fun randomLongFrom1to3() = ThreadLocalRandom.current().nextLong(1, 3)
