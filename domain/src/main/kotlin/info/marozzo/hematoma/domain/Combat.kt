/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.optics.optics
import info.marozzo.hematoma.serializers.PersistentListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Score(private val value: Int) : Comparable<Score> {

    operator fun plus(other: Score) = Score(value + other.value)
    override fun compareTo(other: Score): Int = value.compareTo(other.value)
    override fun toString(): String = "$value\u202FPts"
}

@JvmInline
@Serializable
value class Hits(private val value: UInt) : Comparable<Hits> {

    operator fun inc() = Hits(value.inc())
    override fun compareTo(other: Hits): Int = value.compareTo(other.value)
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

    val winner: CompetitorId? = when {
        scoreA > scoreB -> a
        scoreA < scoreB -> b
        else -> null
    }
    val loser: CompetitorId? = when {
        scoreA > scoreB -> a
        scoreA < scoreB -> b
        else -> null
    }
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

