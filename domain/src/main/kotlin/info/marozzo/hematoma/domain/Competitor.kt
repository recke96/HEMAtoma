/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CompetitorId private constructor(private val id: Long) {

    companion object {
        fun initial() = CompetitorId(0L)
        fun next(ids: Iterable<CompetitorId>) = ids.maxByOrNull(CompetitorId::id)?.next() ?: initial()
    }

    fun next() = CompetitorId(id + 1L)
    override fun toString(): String = "CompetitorId[$id]"
}

@JvmInline
@Serializable
value class RegistrationNumber private constructor(val value: String) {
    companion object {
        operator fun invoke(number: String): Validated<RegistrationNumber> = either {
            ensure(number.isNotBlank()) { ValidationError("Registration number mustn't be blank").nel() }
            RegistrationNumber(number)
        }
    }
}

@JvmInline
@Serializable
value class CompetitorName private constructor(val value: String) {
    companion object {
        operator fun invoke(name: String): Validated<CompetitorName> = either {
            ensure(name.isNotBlank()) { ValidationError("Competitor name mustn't be blank").nel() }
            CompetitorName(name)
        }
    }
}

@optics
@Serializable
data class Competitor(val id: CompetitorId, val registration: RegistrationNumber, val name: CompetitorName) {
    internal companion object
}

fun Event.addCompetitor(number: RegistrationNumber, name: CompetitorName): Validated<Event> = either {
    val nextId = CompetitorId.next(competitors.keys)

    val competitor = zipOrAccumulate(
        { ensure(!competitors.containsKey(nextId)) { ValidationError("There already exists a competitor $nextId") } },
        {
            ensure(competitors.values.none { it.registration == number }) {
                ValidationError("There already exists a competitor $number")
            }
        }
    ) { _, _ -> Competitor(nextId, number, name) }

    Event.competitors.modify(this@Event) {
        it.put(competitor.id, competitor)
    }
}

