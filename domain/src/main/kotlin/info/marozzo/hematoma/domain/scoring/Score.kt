/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain.scoring

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Score private constructor(private val value: Int) : Comparable<Score> {

    companion object {
        val zero = Score(0)
        val seven = Score(7)

        fun parse(value: String): Validated<Score> = either {
            val score = ensureNotNull(value.toIntOrNull()) {
                ValidationError("Expected a number, but got $value").nel()
            }
            Score(score)
        }
    }

    operator fun plus(other: Score) = Score(value + other.value)
    operator fun div(other: Score) = CUT(value.toDouble() / other.value.toDouble())
    override fun compareTo(other: Score): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
}

@JvmInline
@Serializable
value class CUT(val value: Double) : Comparable<CUT> {

    companion object {
        private const val PERCENT_SCALE = 100.0
    }

    override fun compareTo(other: CUT): Int = value.compareTo(other.value)
    override fun toString(): String = when {
        value.isInfinite() -> "\u221E"
        value.isNaN() -> ""
        else -> "%.2f\u202F%%".format(value * PERCENT_SCALE)
    }
}

@JvmInline
@Serializable
value class Hits(private val value: UInt) : Comparable<Hits> {

    companion object {
        val none = Hits(0U)
        val three = Hits(3U)

        fun parse(value: String): Validated<Hits> = either {
            val hits = ensureNotNull(value.toUIntOrNull()) {
                ValidationError("Expected a positive number, but got $value").nel()
            }
            Hits(hits)
        }
    }

    fun half() = Hits(value / 2U)
    operator fun plus(other: Hits) = Hits(value + other.value)
    override fun compareTo(other: Hits): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()
}
