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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
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

    fun getResults(): ImmutableMap<CompetitorId, Result> = when {
        doubleHits >= Hits.three -> persistentMapOf(a to Result.doubleHit, b to Result.doubleHit)
        else -> persistentMapOf(
            a to Result(
                Matches.one,
                if (scoreA > scoreB) Matches.one else Matches.none,
                if (scoreA < scoreB) Matches.one else Matches.none,
                scoreA,
                scoreB,
                doubleHits
            ),
            b to Result(
                Matches.one,
                if (scoreB > scoreA) Matches.one else Matches.none,
                if (scoreB < scoreA) Matches.one else Matches.none,
                scoreB,
                scoreA,
                doubleHits
            )
        )
    }
}



