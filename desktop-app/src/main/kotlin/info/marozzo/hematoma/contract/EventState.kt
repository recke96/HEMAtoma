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
import info.marozzo.hematoma.domain.scoring.FiorDellaSpadaScoring
import kotlinx.collections.immutable.persistentMapOf
import java.nio.file.Path

@optics
@Immutable
data class EventState(
    val path: Path? = null,
    val event: Event = Event(
        EventName("Event").getOrNull()!!,
        persistentMapOf(),
        TournamentId.initial().let {
            persistentMapOf(
                it to Tournament(
                    it,
                    TournamentName("Tournament").getOrNull()!!,
                    FiorDellaSpadaScoring()
                )
            )
        }
    ),
    val screen: Screen = Screen.Configuration
) {
    internal companion object
}

enum class Screen {
    Configuration,
    Competitors,
    Scoring
}
