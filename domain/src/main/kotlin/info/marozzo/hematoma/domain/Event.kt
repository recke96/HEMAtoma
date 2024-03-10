/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.optics.dsl.index
import arrow.optics.optics
import arrow.optics.typeclasses.Index
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.domain.scoring.FiorDellaSpadaScoring
import info.marozzo.hematoma.domain.scoring.Score
import info.marozzo.hematoma.serializers.CompetitorsSerializer
import info.marozzo.hematoma.serializers.TournamentsSerializer
import info.marozzo.hematoma.util.persistentMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class EventName private constructor(private val value: String) {
    companion object {
        operator fun invoke(name: String): Validated<EventName> = either {
            ensure(name.isNotBlank()) { ValidationError("Event name mustn't be empty").nel() }
            EventName(name)
        }
    }

    override fun toString(): String = value
}

@optics
@Serializable
data class Event(
    val name: EventName,
    @Serializable(with = CompetitorsSerializer::class)
    val competitors: PersistentMap<CompetitorId, Competitor>,
    @Serializable(with = TournamentsSerializer::class)
    val tournaments: PersistentMap<TournamentId, Tournament>
) {
    internal companion object

    fun registerCompetitorForTournament(competitor: CompetitorId, tournament: TournamentId): Validated<Event> = either {
        Event.tournaments.index(Index.persistentMap(), tournament).registered.modifyNullable(this@Event) {
            it.add(competitor)
        } ?: raise(ValidationError("No tournament $tournament").nel())
    }

    fun registerCombatForTournament(tournament: TournamentId, combat: Combat): Validated<Event> = either {
        ensure(tournaments.containsKey(tournament)) { ValidationError("No tournament with id $tournament").nel() }
        Event.tournaments.index(Index.persistentMap(), tournament).modifyNullable(this@Event) {
            it.registerCombat(combat).bind()
        } ?: raise(ValidationError("No tournament $tournament").nel())
    }

    fun setWinningThreshold(tournament: TournamentId, threshold: Score): Validated<Event> = either {
        ensure(tournaments.containsKey(tournament)) { ValidationError("No tournament with id $tournament").nel() }
        Event.tournaments.index(Index.persistentMap(), tournament).scoringSettings.modifyNullable(this@Event) {
            when (it) {
                is FiorDellaSpadaScoring -> it.copy(winningThreshold = threshold)
            }
        } ?: raise(ValidationError("No tournament $tournament").nel())
    }
}
