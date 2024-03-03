/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.serializers.PersistentListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Score(private val value: Int) : Comparable<Score> {

    companion object {
        val zero = Score(0)
        val seven = Score(7)

        operator fun invoke(value: String): Validated<Score> = either {
            val score = ensureNotNull(value.toIntOrNull()) {
                ValidationError(
                    "Expected a number, but got $value",
                    "value"
                ).nel()
            }
            Score(score)
        }
    }

    operator fun plus(other: Score) = Score(value + other.value)
    operator fun div(other: Score) = value.toDouble() / other.value.toDouble()
    override fun compareTo(other: Score): Int = value.compareTo(other.value)
    override fun toString(): String = "$value\u202FPts"
}

@JvmInline
@Serializable
value class Hits(private val value: UInt) : Comparable<Hits> {

    companion object {
        val three = Hits(3U)

        operator fun invoke(value: String): Validated<Hits> = either {
            val hits = ensureNotNull(value.toUIntOrNull()) {
                ValidationError(
                    "Expected a positive number, but got $value",
                    "value"
                ).nel()
            }
            Hits(hits)
        }
    }

    operator fun plus(other: Hits) = Hits(value + other.value)
    override fun compareTo(other: Hits): Int = value.compareTo(other.value)

    override fun toString(): String = "$value"
}


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
            Result(a, Matches.one, scoreA, scoreB, doubleHits),
            Result(b, Matches.one, scoreB, scoreA, doubleHits)
        )
    }

    data class CombatResults(val a: Result, val b: Result) : Iterable<Result> by persistentListOf(a, b)
}

@JvmInline
@Serializable
value class Combats private constructor(
    @Serializable(with = PersistentListSerializer::class)
    private val combats: PersistentList<Combat>
) : List<Combat> by combats {

    companion object {
        operator fun invoke() = Combats(persistentListOf())
    }

    fun add(combat: Combat): Combats = Combats(combats.add(combat))
}

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
    val scored: Score = Score(0),
    val conceded: Score = Score(0),
    val doubleHits: Hits = Hits(0U)
) {
    companion object {
        fun doubleHit(competitor: CompetitorId) = Result(
            competitor,
            Matches.one,
            Score.zero,
            Score.seven,
            Hits.three
        )
    }

    operator fun plus(other: Result): Validated<Result> = either {
        ensure(competitor == other.competitor) {
            ValidationError(
                "Can't combine results of different competitors: $competitor, ${other.competitor}",
                "other"
            ).nel()
        }
        Result(
            competitor,
            matches + other.matches,
            scored + other.scored,
            conceded + other.conceded,
            doubleHits + other.doubleHits
        )
    }
}
