/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import arrow.optics.copy
import info.marozzo.hematoma.EventContract
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.event
import info.marozzo.hematoma.path
import info.marozzo.hematoma.utils.readFromFile
import kotlinx.serialization.json.Json

suspend fun EventInputHandlerScope.handleOpenFile(input: EventContract.Input.OpenFile): Unit {
    val result = Json.readFromFile<Event>(input.path)

    result.onRight { event ->
        updateState {
            it.copy {
                EventContract.State.path set input.path
                EventContract.State.event set event
            }
        }
    }
}
