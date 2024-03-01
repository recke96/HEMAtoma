/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.runtime.Immutable
import arrow.optics.optics
import info.marozzo.hematoma.domain.Competitors
import info.marozzo.hematoma.domain.Event
import java.nio.file.Path

object EventContract {

    @optics
    @Immutable
    data class State(
        val path: Path? = null,
        val event: Event = Event("", Competitors()),
        val errors: List<String> = emptyList()
    ) {
        companion object
    }

    sealed interface Input {
        data class OpenFile(val path: Path) : Input
        data class SaveAs(val path: Path) : Input
        data object Save : Input
    }
}
