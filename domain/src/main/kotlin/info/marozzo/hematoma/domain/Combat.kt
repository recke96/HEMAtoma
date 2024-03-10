/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain

import arrow.optics.optics
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Score
import kotlinx.serialization.Serializable

@optics
@Serializable
data class Combat(
    val a: CompetitorId,
    val b: CompetitorId,
    val scoreA: Score,
    val scoreB: Score,
    val doubleHits: Hits
) {
    internal companion object
}



