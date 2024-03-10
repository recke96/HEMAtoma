/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain.scoring

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.optics.optics
import info.marozzo.hematoma.domain.CompetitorId
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
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
    val competitor: CompetitorId,
    val matches: Matches = Matches.none,
    val wins: Matches = Matches.none,
    val losses: Matches = Matches.none,
    val scored: Score = Score.zero,
    val conceded: Score = Score.zero,
    val doubleHits: Hits = Hits.none
) {
    val cut = scored / conceded

    companion object {
        fun doubleHit(competitor: CompetitorId) = Result(
            competitor,
            matches = Matches.one,
            losses = Matches.one,
            scored = Score.zero,
            conceded = Score.seven,
            doubleHits = Hits.three
        )
    }

    operator fun plus(other: Result): Validated<Result> = either {
        ensure(competitor == other.competitor) {
            ValidationError(
                "Can't combine results of different competitors: $competitor, ${other.competitor}"
            ).nel()
        }
        Result(
            competitor,
            matches + other.matches,
            wins + other.wins,
            losses + other.losses,
            scored + other.scored,
            conceded + other.conceded,
            doubleHits + other.doubleHits
        )
    }
}
