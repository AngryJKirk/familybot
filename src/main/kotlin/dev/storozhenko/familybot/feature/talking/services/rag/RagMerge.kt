package dev.storozhenko.familybot.feature.talking.services.rag

import dev.storozhenko.familybot.feature.talking.models.Kind
import dev.storozhenko.familybot.feature.talking.models.RagHit
import java.time.Duration
import java.time.Instant
import kotlin.math.exp

fun mergeAndRerank(
    semantic: List<RagHit>,
    keywordRu: List<RagHit>,
    keywordSimple: List<RagHit> = emptyList(),
    recent: List<RagHit> = emptyList(),
    fuzzy: List<RagHit> = emptyList(),
    maxResults: Int = 50,
    shortQuery: Boolean = false,
): List<RagHit> {
    val now = Instant.now()


    val byIdRep = mutableMapOf<Long, RagHit>()
    (semantic + keywordRu + keywordSimple + recent + fuzzy).forEach { hit ->
        byIdRep[hit.ragId] = pickRep(byIdRep[hit.ragId], hit)
    }

    val semMap = semantic.associate { it.ragId to it.score }
    val kwMap = (keywordRu + keywordSimple)
        .groupBy(RagHit::ragId)
        .mapValues { (_, v) -> v.maxOf(RagHit::score) }
    val fuzzyMap = fuzzy.associate { it.ragId to it.score }

    val recMap = byIdRep.mapValues { (_, rep) ->
        val ts = rep.ts
        val minutesSince = Duration.between(ts, now).toMinutes().toDouble()
        exp(-minutesSince / 720.0) // ~12h time constant
    }


    val semN = normalize(semMap)
    val kwN = normalize(kwMap)
    val fuzzyN = normalize(fuzzyMap)

    val (wSem, wKw, wFz, wRec) = if (shortQuery) {
        Quadruple(0.30, 0.30, 0.10, 0.30)
    } else {
        Quadruple(0.60, 0.25, 0.05, 0.10)
    }


    val agg = mutableMapOf<Long, Agg>()
    for ((ragId, _) in byIdRep) {
        val s = semN[ragId] ?: 0.0
        val k = kwN[ragId] ?: 0.0
        val fz = fuzzyN[ragId] ?: 0.0
        val rc = recMap[ragId] ?: 0.0

        val semPart = wSem * s
        val kwPart = wKw * k
        val fzPart = wFz * fz
        val rcPart = wRec * rc
        val total = semPart + kwPart + fzPart + rcPart

        val topKind = when {
            semPart >= kwPart && semPart >= fzPart && semPart >= rcPart -> Kind.SEMANTIC
            kwPart >= semPart && kwPart >= fzPart && kwPart >= rcPart -> Kind.KEYWORD_RU
            fzPart >= semPart && fzPart >= kwPart && fzPart >= rcPart -> Kind.FUZZY
            else -> Kind.RECENT
        }
        agg[ragId] = Agg(total, topKind)
    }

    // Rank by aggregate score, take top N, then sort chronologically for readability
    val top = agg.entries
        .sortedByDescending { it.value.score }
        .take(maxResults)
        .map { (ragId, a) ->
            val rep = byIdRep[ragId] ?: throw RuntimeException("Failed to merge RAG: $agg")
            rep.copy(score = a.score, kind = a.topKind)
        }
        .sortedBy { it.ts }

    return top
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Agg(val score: Double, val topKind: Kind)

private fun normalize(map: Map<Long, Double>): Map<Long, Double> {
    if (map.isEmpty()) return emptyMap()
    val min = map.values.min()
    val max = map.values.max()
    return if (max > min) map.mapValues { (it.value - min) / (max - min) }
    else map.mapValues { if (it.value > 0.0) 1.0 else 0.0 }
}

private fun pickRep(a: RagHit?, b: RagHit): RagHit = when {
    a == null -> b
    a.kind == Kind.SEMANTIC -> a
    b.kind == Kind.SEMANTIC -> b
    a.kind == Kind.KEYWORD_RU || a.kind == Kind.KEYWORD_SIMPLE -> a
    b.kind == Kind.KEYWORD_RU || b.kind == Kind.KEYWORD_SIMPLE -> b
    a.kind == Kind.RECENT -> a
    b.kind == Kind.RECENT -> b
    else -> a
}