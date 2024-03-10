/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain.scoring

import arrow.optics.optics
import info.marozzo.hematoma.domain.Combat
import info.marozzo.hematoma.domain.CompetitorId
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Serializable
sealed interface ScoringSettings {
    fun resultOfCombat(combat: Combat): ImmutableMap<CompetitorId, Result>
    fun punishmentResult(): Result

    fun isPunished(combat: Combat): Boolean
}

@optics
@Serializable
@SerialName("fior-della-spada-scoring")
data class FiorDellaSpadaScoring(val winningThreshold: Score = DEFAULT_WINNING_THRESHOLD) : ScoringSettings {

    companion object {
        private const val MAX_AWARDED = 3.0
        private val DEFAULT_WINNING_THRESHOLD = Score(7)
    }

    private val doubleHitThreshold = Hits(ceil(winningThreshold.value / MAX_AWARDED).toUInt() - 1U)

    override fun resultOfCombat(combat: Combat): ImmutableMap<CompetitorId, Result> = with(combat) {
        when {
            doubleHits > doubleHitThreshold -> persistentMapOf(
                a to punishmentResult(),
                b to punishmentResult()
            )

            else -> persistentMapOf(
                a to Result(
                    matches = Matches.one,
                    wins = if (scoreA > scoreB) Matches.one else Matches.none,
                    losses = if (scoreA < scoreB) Matches.one else Matches.none,
                    scored = scoreA,
                    conceded = scoreB,
                    doubleHits = doubleHits
                ),
                b to Result(
                    matches = Matches.one,
                    wins = if (scoreB > scoreA) Matches.one else Matches.none,
                    losses = if (scoreB < scoreA) Matches.one else Matches.none,
                    scored = scoreB,
                    conceded = scoreA,
                    doubleHits = doubleHits
                )
            )
        }
    }

    override fun punishmentResult(): Result = Result(
        matches = Matches.one,
        wins = Matches.none,
        losses = Matches.one,
        scored = Score.zero,
        conceded = winningThreshold,
        doubleHits = doubleHitThreshold + Hits.one
    )

    override fun isPunished(combat: Combat): Boolean = combat.doubleHits > doubleHitThreshold
}
