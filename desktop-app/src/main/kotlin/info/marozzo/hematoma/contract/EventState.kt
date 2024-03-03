/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

import androidx.compose.runtime.Immutable
import arrow.optics.optics
import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.domain.Event
import kotlinx.collections.immutable.persistentListOf
import java.nio.file.Path

@optics
@Immutable
data class EventState(
    val path: Path? = null,
    val event: Event = Event(
        "Event", Competitors(), Tournaments(
            persistentListOf(
                Tournament(TournamentName("Tournament").getOrNull()!!)
            )
        )
    ),
    val screen: Screen = Screen.Competitors
) {
    companion object
}

enum class Screen {
    Competitors,
    Scoring
}
