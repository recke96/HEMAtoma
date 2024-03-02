/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.mapOrAccumulate
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.serializers.PersistentListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CompetitorId private constructor(private val id: Long) : Comparable<CompetitorId> {

    companion object {
        fun initial() = CompetitorId(0L)
    }

    override fun compareTo(other: CompetitorId): Int = id.compareTo(other.id)
    fun next() = CompetitorId(id + 1L)
}

@JvmInline
@Serializable
value class RegistrationNumber private constructor(val value: String) {
    companion object {
        operator fun invoke(number: String): Validated<RegistrationNumber> = either {
            ensure(number.isNotBlank()) { ValidationError("Registration number mustn't be blank", "number").nel() }
            RegistrationNumber(number)
        }
    }
}

@JvmInline
@Serializable
value class CompetitorName private constructor(val value: String) {
    companion object {
        operator fun invoke(name: String): Validated<CompetitorName> = either {
            ensure(name.isNotBlank()) { ValidationError("Competitor name mustn't be blank", "name").nel() }
            CompetitorName(name)
        }
    }
}

@optics
@Serializable
data class Competitor(val id: CompetitorId, val registration: RegistrationNumber, val name: CompetitorName) {
    companion object
}

@JvmInline
@Serializable
value class Competitors private constructor(
    @Serializable(with = PersistentListSerializer::class)
    private val competitors: PersistentList<Competitor>
) : List<Competitor> by competitors {

    companion object {
        operator fun invoke() = Competitors(persistentListOf())
    }

    fun add(competitor: Competitor): Validated<Competitors> = either {
        mapOrAccumulate(competitors) {
            ensure(it.id != competitor.id) {
                ValidationError(
                    "There already is a competitor with id ${competitor.id}",
                    "competitor"
                )
            }
            ensure(it.registration != competitor.registration) {
                ValidationError(
                    "There already is a competitor with registration ${competitor.registration}",
                    "competitor"
                )
            }
            competitor
        }

        Competitors(competitors.add(competitor))
    }
}
