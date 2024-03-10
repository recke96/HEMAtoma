/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain.scoring

import arrow.optics.optics
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Matches(val value: UInt) : Comparable<Matches> {

    companion object {
        val none = Matches(0U)
        val one = Matches(1U)
    }

    operator fun plus(other: Matches) = Matches(value + other.value)
    override fun compareTo(other: Matches): Int = value.compareTo(other.value)
}

@optics
@Serializable
data class Result(
    val matches: Matches,
    val wins: Matches,
    val losses: Matches,
    val scored: Score,
    val conceded: Score,
    val doubleHits: Hits
) {
    val cut = scored / conceded

    companion object {
        val empty = Result(
            matches = Matches.none,
            wins = Matches.none,
            losses = Matches.none,
            scored = Score.zero,
            conceded = Score.zero,
            doubleHits = Hits.none
        )
        val doubleHit = Result(
            matches = Matches.one,
            wins = Matches.none,
            losses = Matches.one,
            scored = Score.zero,
            conceded = Score.seven,
            doubleHits = Hits.three
        )
    }

    operator fun plus(other: Result) = Result(
        matches + other.matches,
        wins + other.wins,
        losses + other.losses,
        scored + other.scored,
        conceded + other.conceded,
        doubleHits + other.doubleHits
    )
}
