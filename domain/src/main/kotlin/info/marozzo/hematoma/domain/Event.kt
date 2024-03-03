/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.raise.either
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import kotlinx.serialization.Serializable

@optics
@Serializable
data class Event(val name: String, val competitors: Competitors, val tournaments: Tournaments) {
    companion object

    fun addCompetitor(number: RegistrationNumber, name: CompetitorName): Validated<Event> = either {
        val nextId = competitors.maxOfOrNull(Competitor::id)?.next() ?: CompetitorId.initial()
        val competitor = Competitor(nextId, number, name)

        Event.competitors.modify(this@Event) {
            it.add(competitor).bind()
        }
    }
}
