/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import arrow.optics.copy
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.EventContract
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.event
import info.marozzo.hematoma.path
import info.marozzo.hematoma.utils.readFromFile
import kotlinx.serialization.json.Json

data object OpenFileHandler  {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: EventContract.Input.OpenFile) {
        sideJob("read-file-${input.path}") {
            Json.readFromFile<Event>(input.path).fold(
                { flogger.atInfo().log("Error reading file %s: %s", input.path, it) },
                { postInput(EventContract.Input.OpenedFile(input.path, it)) }
            )
        }
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: EventContract.Input.OpenedFile) {
        updateState {
            it.copy {
                EventContract.State.path set input.path
                EventContract.State.event set input.event
            }
        }
    }
}
