package dev.storozhenko.familybot.common.extensions

import org.tomlj.Toml
import org.tomlj.TomlParseResult
import dev.storozhenko.familybot.telegram.FamilyBot
import java.util.concurrent.ThreadLocalRandom

fun randomInt(from: Int, to: Int) = ThreadLocalRandom.current().nextInt(from, to)
fun randomLong(from: Long, to: Long) = ThreadLocalRandom.current().nextLong(from, to)

fun randomBoolean(probability: Long? = null): Boolean {
    return if (probability == null) {
        ThreadLocalRandom.current().nextBoolean()
    } else {
        ThreadLocalRandom.current().nextLong(0, probability) == 0L
    }
}

fun readTomlFromStatic(filename: String): TomlParseResult {
    val resourceAsStream = FamilyBot::class.java.classLoader
        .getResourceAsStream("static/$filename")
        ?: throw FamilyBot.InternalException("$filename is missing")

    return Toml.parse(resourceAsStream)
}
