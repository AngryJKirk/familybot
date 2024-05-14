package dev.storozhenko.familybot.feature.talking.services

import dev.storozhenko.familybot.common.extensions.readTomlFromStatic
import dev.storozhenko.familybot.core.telegram.FamilyBot
import org.springframework.stereotype.Component
import org.tomlj.TomlTable

@Component
class GptSettingsReader {
    private val universes: Map<GptUniverse, String>
    private val styles: Map<GptStyle, String>

    companion object {
        private const val UNIVERSE_TABLE = "UNIVERSE"
        private const val STYLE_TABLE = "STYLE"
    }

    init {
        val toml = readTomlFromStatic("gpt.toml")
        universes = getTable(toml, UNIVERSE_TABLE)
            .toMap()
            .map { (key, value) -> GptUniverse.valueOf(key) to value as String }
            .toMap()

        styles = getTable(toml, STYLE_TABLE)
            .toMap()
            .map { (key, value) -> GptStyle.valueOf(key) to value as String }
            .toMap()
    }

    private fun getTable(toml: TomlTable, name: String): TomlTable {
        return toml.getTable(name) ?: throw FamilyBot.InternalException("No $name table in gpt.toml")
    }

    fun getUniverseValue(universe: GptUniverse): String {
        return universes[universe] ?: ""
    }

    fun getStyleValue(style: GptStyle): String {
        return styles[style] ?: ""
    }
}

enum class GptStyle(val value: String, val universe: GptUniverse = GptUniverse.DEFAULT) {
    RUDE("грубый"),
    CUTE("милый"),
    SEXY("сексуальный"),
    HELPING("прислуживающий"),
    NEUTRAL("нейтральный"),
    ASSISTANT("ассистент", GptUniverse.ASSISTANT);

    companion object {
        private val lookUpMap = entries.associateBy(GptStyle::value)
        fun lookUp(value: String) = lookUpMap[value]
    }
}

enum class GptUniverse {
    DEFAULT,
    ASSISTANT,
    GPT4
}
