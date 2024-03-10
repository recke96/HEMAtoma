/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.getOrElse
import arrow.core.nel
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.serializers.PersistentListSerializer
import info.marozzo.hematoma.serializers.PersistentSetSerializer
import kotlinx.collections.immutable.*
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class TournamentId private constructor(private val id: Long) {

    companion object {
        fun initial() = TournamentId(0L)
        fun next(ids: Iterable<TournamentId>) = ids.maxByOrNull(TournamentId::id)?.next() ?: initial()
    }

    fun next() = TournamentId(id + 1L)
    override fun toString(): String = "TournamentId[$id]"
}

@JvmInline
@Serializable
value class TournamentName private constructor(val value: String) {
    companion object {
        operator fun invoke(name: String): Validated<TournamentName> = either {
            ensure(name.isNotBlank()) { ValidationError("Tournament name mustn't be empty").nel() }
            TournamentName(name)
        }
    }

    override fun toString(): String = value
}

@optics
@Serializable
data class Tournament(
    val id: TournamentId,
    val name: TournamentName,
    @Serializable(with = PersistentSetSerializer::class)
    val registered: PersistentSet<CompetitorId> = persistentSetOf(),
    @Serializable(with = PersistentListSerializer::class)
    val record: PersistentList<Combat> = persistentListOf()
) {
    internal companion object

    fun getResults(): ImmutableMap<CompetitorId, Result> {
        // Used so that even competitors which have no record yet are in the result
        val zeroResults = registered.asSequence().map(::Result)
        val combatResults = record.asSequence().flatMap(Combat::getResults)

        return zeroResults.plus(combatResults)
            .groupingBy(Result::competitor)
            .aggregateTo(persistentMapOf<CompetitorId, Result>().builder()) { _, total, result, _ ->
                total?.let { total.plus(result).getOrElse { total } } ?: result
            }.build()
    }

    fun registerCombat(combat: Combat): Validated<Tournament> = either {
        zipOrAccumulate(
            { ensureRegistered(combat.b) },
            { ensureRegistered(combat.b) }
        ) { _, _ ->
            Tournament.record.modify(this@Tournament) {
                it.add(combat)
            }
        }
    }

    private fun Raise<ValidationError>.ensureRegistered(competitor: CompetitorId) =
        ensure(registered.contains(competitor)) {
            ValidationError("Competitor $competitor is not registered for tournament $name")
        }
}
