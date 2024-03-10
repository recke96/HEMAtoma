/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.optics.optics
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Matches
import info.marozzo.hematoma.domain.scoring.Result
import info.marozzo.hematoma.domain.scoring.Score
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@optics
@Serializable
data class Combat(
    val a: CompetitorId,
    val b: CompetitorId,
    val scoreA: Score,
    val scoreB: Score,
    val doubleHits: Hits
) {
    companion object

    fun getResults(): CombatResults = when {
        doubleHits >= Hits.three -> CombatResults(Result.doubleHit(a), Result.doubleHit(b))
        else -> CombatResults(
            Result(
                a,
                Matches.one,
                if (scoreA > scoreB) Matches.one else Matches.none,
                if (scoreA < scoreB) Matches.one else Matches.none,
                scoreA,
                scoreB,
                doubleHits
            ),
            Result(
                b,
                Matches.one,
                if (scoreB > scoreA) Matches.one else Matches.none,
                if (scoreB < scoreA) Matches.one else Matches.none,
                scoreB,
                scoreA,
                doubleHits
            )
        )
    }

    data class CombatResults(val a: Result, val b: Result) : Iterable<Result> by persistentListOf(a, b)
}



