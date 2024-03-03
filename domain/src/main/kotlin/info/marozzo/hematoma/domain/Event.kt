/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import info.marozzo.hematoma.domain.errors.ValidationError
import kotlinx.serialization.Serializable

@optics
@Serializable
data class Event(val name: String, val competitors: Competitors, val tournaments: Tournaments) {
    companion object

    fun addCompetitor(number: RegistrationNumber, name: CompetitorName): Validated<Event> = either {
        val nextId = CompetitorId.next(competitors.map(Competitor::id))
        val competitor = Competitor(nextId, number, name)

        Event.competitors.modify(this@Event) {
            it.add(competitor).bind()
        }
    }

    fun registerCompetitorForTournament(competitor: CompetitorId, tournament: TournamentId): Validated<Event> = either {
        zipOrAccumulate(
            {
                ensure(competitors.any { it.id == competitor }) {
                    ValidationError(
                        "No comppetitor with id $competitor",
                        "competitor"
                    )
                }
            },
            {
                ensure(tournaments.any { it.id == tournament }) {
                    ValidationError(
                        "No tournament with id $tournament",
                        "tournament"
                    )
                }
            }
        ) { _, _ ->
            Event.tournaments.modify(this@Event) {
                it.register(tournament, competitor).bind()
            }
        }
    }
}
