/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.nel
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import arrow.core.right
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
}

@JvmInline
@Serializable
value class TournamentName private constructor(val value: String) {
    companion object {
        operator fun invoke(name: String): Validated<TournamentName> = either {
            ensure(name.isNotBlank()) { ValidationError("Tournament name mustn't be empty", "name").nel() }
            TournamentName(name)
        }
    }
}

@optics
@Serializable
data class Tournament(
    val id: TournamentId,
    val name: TournamentName,
    @Serializable(with = PersistentSetSerializer::class)
    val registered: PersistentSet<CompetitorId> = persistentSetOf(),
    val record: Combats = Combats()
) {
    companion object

    fun registerCompetitor(competitor: CompetitorId): Validated<Tournament> =
        Tournament.registered.modify(this) { it.add(competitor) }.right()

    fun registerCombat(combat: Combat): Validated<Tournament> = either {
        zipOrAccumulate(
            { ensureRegistered(combat.b, "combat") },
            { ensureRegistered(combat.b, "combat") }
        ) { _, _ ->
            Tournament.record.modify(this@Tournament) {
                it.add(combat)
            }
        }
    }

    private fun Raise<ValidationError>.ensureRegistered(competitor: CompetitorId, property: String) =
        ensure(registered.contains(competitor)) {
            ValidationError(
                "Competitor $competitor is not registered for tournament ${name.value}",
                property
            )
        }
}

@JvmInline
@Serializable
value class Tournaments(
    @Serializable(with = PersistentListSerializer::class)
    private val value: PersistentList<Tournament> = persistentListOf()
) : List<Tournament> by value {
    companion object

    fun registerCompetitor(tournamentId: TournamentId, competitorId: CompetitorId): Validated<Tournaments> = either {
        value.mutate {
            it.replaceAll { tournament ->
                if (tournament.id == tournamentId) {
                    tournament.registerCompetitor(competitorId).bind()
                } else {
                    tournament
                }
            }
        }.let(::Tournaments)
    }

    fun registerCombat(tournamentId: TournamentId, combat: Combat): Validated<Tournaments> = either {
        value.mutate {
            it.replaceAll { tournament ->
                if (tournament.id == tournamentId) {
                    tournament.registerCombat(combat).bind()
                } else {
                    tournament
                }
            }
        }.let(::Tournaments)
    }
}
