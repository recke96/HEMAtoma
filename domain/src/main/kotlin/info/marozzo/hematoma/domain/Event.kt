/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import arrow.optics.optics
import info.marozzo.hematoma.domain.errors.Validated
import kotlinx.serialization.Serializable

@optics
@Serializable
data class Event(val name: String, val competitors: Competitors) {
    companion object

    fun addCompetitor(name: String): Validated<Event> = either {
        val nextId = competitors.maxOfOrNull(Competitor::id)?.next() ?: CompetitorId.initial()
        val competitor = zipOrAccumulate(
            { RegistrationNumber(nextId.toString()).bind() },
            { CompetitorName(name).bind() },
            { reg, name -> Competitor(nextId, reg, name) }
        )

        Event.competitors.modify(this@Event) {
            it.add(competitor).bind()
        }
    }
}
