package space.yaroslav.familybot.repos

import org.springframework.stereotype.Component
import space.yaroslav.familybot.common.random


@Component
class InCodeKeywordRepository : KeywordRepository {

    private val keyset: Map<Set<String>, Set<String>> = mapOf(
            setOf("Java") to setOf("C# sucks, Java rulezzz")
    )


    override fun getKeywords(): Set<String> {
        return keyset.keys.flatMap { it } .toSet()
    }

    override fun getPhrasesByKeyword(key: String): Set<String> {
        val filter = keyset.filter { it.key.contains(key) }
        return filter.filter { it.key.contains(key) }.map { it.value }.random()
    }
}